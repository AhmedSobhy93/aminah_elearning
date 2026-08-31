package com.aminah.elearning.service;

import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @Test
    void profileUpdateUsesPrincipalAndKeepsPasswordWhenBlank() {
        UserRepository users = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        UserService service = new UserService(users, encoder);
        User current = new User(7L);
        current.setUsername("student");
        current.setEmail("old@example.com");
        current.setPassword("existing-hash");
        User submitted = new User(999L);
        submitted.setFullName("Updated Name");
        submitted.setEmail("new@example.com");
        submitted.setPassword(" ");
        when(users.findByUsername("student")).thenReturn(Optional.of(current));
        when(users.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(users.save(current)).thenReturn(current);

        User saved = service.userUpdate("student", submitted);

        assertThat(saved.getId()).isEqualTo(7L);
        assertThat(saved.getEmail()).isEqualTo("new@example.com");
        assertThat(saved.getPassword()).isEqualTo("existing-hash");
        verify(encoder, never()).encode(" ");
    }

    @Test
    void duplicateEmailIsRejectedBeforeProfileMutation() {
        UserRepository users = mock(UserRepository.class);
        UserService service = new UserService(users, mock(PasswordEncoder.class));
        User current = new User(7L);
        current.setFullName("Original Name");
        current.setEmail("old@example.com");
        current.setPassword("existing-hash");
        User other = new User(8L);
        User submitted = new User();
        submitted.setFullName("Mutated Name");
        submitted.setEmail("taken@example.com");
        submitted.setPassword("new-password");
        when(users.findByUsername("student")).thenReturn(Optional.of(current));
        when(users.findByEmail("taken@example.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.userUpdate("student", submitted))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is already in use");

        assertThat(current.getFullName()).isEqualTo("Original Name");
        assertThat(current.getEmail()).isEqualTo("old@example.com");
        assertThat(current.getPassword()).isEqualTo("existing-hash");
        verify(users, never()).save(current);
    }

    @Test
    void profileEmailIsCanonicalizedBeforeDuplicateCheck() {
        UserRepository users = mock(UserRepository.class);
        UserService service = new UserService(users, mock(PasswordEncoder.class));
        User current = new User(7L);
        current.setEmail("old@example.com");
        User other = new User(8L);
        User submitted = new User();
        submitted.setEmail(" Student@Example.com ");
        when(users.findByUsername("student")).thenReturn(Optional.of(current));
        when(users.findByEmail("student@example.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.userUpdate("student", submitted))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is already in use");

        verify(users).findByEmail("student@example.com");
        verify(users, never()).save(current);
    }
}
