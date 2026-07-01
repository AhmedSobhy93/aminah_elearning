package com.aminah.elearning.config;

import com.aminah.elearning.model.Role;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminBootstrapRunnerTest {

    @Test
    void createsInitialAdminWhenNoAdminExists() {
        UserRepository userRepository = mock(UserRepository.class);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        AdminBootstrapRunner runner = new AdminBootstrapRunner(userRepository, passwordEncoder);
        configure(runner, "admin", "admin@example.com", "very-secure-password", "Admin User");

        when(userRepository.countByRole(Role.ADMIN)).thenReturn(0L);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.empty());

        runner.run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("admin");
        assertThat(saved.getEmail()).isEqualTo("admin@example.com");
        assertThat(saved.getFullName()).isEqualTo("Admin User");
        assertThat(saved.getRole()).isEqualTo(Role.ADMIN);
        assertThat(saved.isEnabled()).isTrue();
        assertThat(passwordEncoder.matches("very-secure-password", saved.getPassword())).isTrue();
    }

    @Test
    void skipsBootstrapWhenAdminAlreadyExists() {
        UserRepository userRepository = mock(UserRepository.class);
        AdminBootstrapRunner runner = new AdminBootstrapRunner(userRepository, new BCryptPasswordEncoder());
        configure(runner, "admin", "admin@example.com", "very-secure-password", "Admin User");

        when(userRepository.countByRole(Role.ADMIN)).thenReturn(1L);

        runner.run();

        verify(userRepository, never()).save(any());
    }

    private void configure(
            AdminBootstrapRunner runner,
            String username,
            String email,
            String password,
            String fullName
    ) {
        ReflectionTestUtils.setField(runner, "username", username);
        ReflectionTestUtils.setField(runner, "email", email);
        ReflectionTestUtils.setField(runner, "password", password);
        ReflectionTestUtils.setField(runner, "fullName", fullName);
    }
}
