package com.aminah.elearning.controller;

import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.UserRepository;
import com.aminah.elearning.repository.VerificationTokenRepository;
import com.aminah.elearning.service.RegistrationService;
import com.aminah.elearning.service.RequestThrottleService;
import com.aminah.elearning.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProfileControllerTest {

    @Test
    void registrationIpLimitCannotBeBypassedWithFreshEmails() {
        RegistrationService registrations = mock(RegistrationService.class);
        ProfileController controller = new ProfileController(
                mock(UserRepository.class),
                mock(PasswordEncoder.class),
                registrations,
                mock(VerificationTokenRepository.class),
                mock(UserService.class),
                mock(AuthenticationManager.class),
                new RequestThrottleService(),
                "http://localhost:8080"
        );
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("203.0.113.10");

        String sixthResult = null;
        for (int index = 1; index <= 6; index++) {
            User user = new User();
            user.setEmail("student" + index + "@example.com");
            sixthResult = controller.register(user, request, mock(Model.class));
        }

        assertThat(sixthResult).isEqualTo("profile/login");
        verify(registrations, times(5)).register(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.eq("http://localhost:8080"));
    }

    @Test
    void confirmationResendHasIndependentIpLimit() {
        RegistrationService registrations = mock(RegistrationService.class);
        ProfileController controller = new ProfileController(
                mock(UserRepository.class),
                mock(PasswordEncoder.class),
                registrations,
                mock(VerificationTokenRepository.class),
                mock(UserService.class),
                mock(AuthenticationManager.class),
                new RequestThrottleService(),
                "http://localhost:8080"
        );
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("203.0.113.30");

        for (int index = 1; index <= 4; index++) {
            controller.resendConfirmation("student" + index + "@example.com", request, mock(Model.class));
        }

        verify(registrations, times(3)).resendConfirmation(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.eq("http://localhost:8080")
        );
    }

    @Test
    void confirmationResendEmailLimitUsesCanonicalAddress() {
        RegistrationService registrations = mock(RegistrationService.class);
        ProfileController controller = new ProfileController(
                mock(UserRepository.class), mock(PasswordEncoder.class), registrations,
                mock(VerificationTokenRepository.class), mock(UserService.class),
                mock(AuthenticationManager.class), new RequestThrottleService(), "http://localhost:8080"
        );

        String[] variants = {"student@example.com", "Student@Example.com", " student@example.com ", "STUDENT@example.com"};
        for (int index = 0; index < variants.length; index++) {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getRemoteAddr()).thenReturn("203.0.113." + (40 + index));
            controller.resendConfirmation(variants[index], request, mock(Model.class));
        }

        verify(registrations, times(3)).resendConfirmation("student@example.com", "http://localhost:8080");
    }
}
