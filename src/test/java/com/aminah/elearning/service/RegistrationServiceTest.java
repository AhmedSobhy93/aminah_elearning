package com.aminah.elearning.service;

import com.aminah.elearning.exception.DuplicateUserException;
import com.aminah.elearning.model.Role;
import com.aminah.elearning.model.User;
import com.aminah.elearning.model.VerificationToken;
import com.aminah.elearning.repository.UserRepository;
import com.aminah.elearning.repository.VerificationTokenRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistrationServiceTest {

    @Test
    void publicRegistrationNormalizesAndCreatesStudentOnly() {
        UserRepository userRepository = mock(UserRepository.class);
        VerificationTokenRepository tokenRepository = mock(VerificationTokenRepository.class);
        EmailService emailService = mock(EmailService.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        RegistrationService service = new RegistrationService(
                userRepository,
                tokenRepository,
                emailService,
                passwordEncoder
        );

        User user = new User();
        user.setUsername(" student ");
        user.setEmail(" Student@Example.COM ");
        user.setFullName(" Student User ");
        user.setPhoneNumber(" 01000000000 ");
        user.setPassword("plain-password");
        user.setRole(Role.DR);

        when(userRepository.findByUsername("student")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");

        service.register(user, "https://aminah.example.com");

        assertThat(user.getUsername()).isEqualTo("student");
        assertThat(user.getEmail()).isEqualTo("student@example.com");
        assertThat(user.getFullName()).isEqualTo("Student User");
        assertThat(user.getPhoneNumber()).isEqualTo("01000000000");
        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(user.getRole()).isEqualTo(Role.STUDENT);
        assertThat(user.isEnabled()).isFalse();
        verify(userRepository).save(user);
        verify(tokenRepository).save(any(VerificationToken.class));
        verify(emailService).sendEmail(
                eq("student@example.com"),
                eq("Confirm your registration - Aminah E-Learning"),
                contains("https://aminah.example.com/profile/confirm?token=")
        );
    }

    @Test
    void rejectsDuplicateUsernameBeforeSaving() {
        UserRepository userRepository = mock(UserRepository.class);
        RegistrationService service = new RegistrationService(
                userRepository,
                mock(VerificationTokenRepository.class),
                mock(EmailService.class),
                mock(PasswordEncoder.class)
        );
        User existing = new User();
        User user = new User();
        user.setUsername("student");
        user.setEmail("student@example.com");

        when(userRepository.findByUsername("student")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.register(user, "https://aminah.example.com"))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessage("Username is already registered");
        verify(userRepository, never()).save(any());
    }

    @Test
    void rejectsDuplicateEmailBeforeSaving() {
        UserRepository userRepository = mock(UserRepository.class);
        RegistrationService service = new RegistrationService(
                userRepository,
                mock(VerificationTokenRepository.class),
                mock(EmailService.class),
                mock(PasswordEncoder.class)
        );
        User existing = new User();
        User user = new User();
        user.setUsername("student");
        user.setEmail("student@example.com");

        when(userRepository.findByUsername("student")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.register(user, "https://aminah.example.com"))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessage("Email is already registered");
        verify(userRepository, never()).save(any());
    }

    @Test
    void resendsConfirmationForInactiveUserOnly() {
        UserRepository userRepository = mock(UserRepository.class);
        VerificationTokenRepository tokenRepository = mock(VerificationTokenRepository.class);
        EmailService emailService = mock(EmailService.class);
        RegistrationService service = new RegistrationService(
                userRepository,
                tokenRepository,
                emailService,
                mock(PasswordEncoder.class)
        );
        User user = new User();
        user.setEmail("student@example.com");
        user.setEnabled(false);

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(user));

        service.resendConfirmation(" Student@Example.COM ", "https://aminah.example.com");

        verify(tokenRepository).deleteByUser(user);
        ArgumentCaptor<VerificationToken> captor = ArgumentCaptor.forClass(VerificationToken.class);
        verify(tokenRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isSameAs(user);
        verify(emailService).sendEmail(
                eq("student@example.com"),
                eq("Confirm your registration - Aminah E-Learning"),
                contains("https://aminah.example.com/profile/confirm?token=")
        );
    }

    @Test
    void resendConfirmationDoesNothingForEnabledUser() {
        UserRepository userRepository = mock(UserRepository.class);
        VerificationTokenRepository tokenRepository = mock(VerificationTokenRepository.class);
        EmailService emailService = mock(EmailService.class);
        RegistrationService service = new RegistrationService(
                userRepository,
                tokenRepository,
                emailService,
                mock(PasswordEncoder.class)
        );
        User user = new User();
        user.setEnabled(true);

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(user));

        service.resendConfirmation("student@example.com", "https://aminah.example.com");

        verify(tokenRepository, never()).deleteByUser(any());
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendEmail(any(), any(), any());
    }
}
