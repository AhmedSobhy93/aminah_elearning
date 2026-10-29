package com.aminah.elearning.service;

import com.aminah.elearning.model.Role;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminAccountSafetyTest {
    private User admin(long id, boolean enabled) {
        User user = new User(id);
        user.setRole(Role.ADMIN);
        user.setEnabled(enabled);
        user.setFullName("QA Admin");
        user.setEmail("admin" + id + "@example.test");
        return user;
    }

    @Test
    void lastActiveAdminCannotBeDisabledDemotedOrDeletedEvenWithAnInactiveAdmin() {
        for (String action : List.of("disable", "demote", "delete")) {
            UserRepository users = mock(UserRepository.class);
            UserService service = new UserService(users, mock(PasswordEncoder.class));
            User current = admin(1L, true);
            when(users.findById(1L)).thenReturn(Optional.of(current));
            when(users.findByRoleForUpdate(Role.ADMIN)).thenReturn(List.of(current, admin(2L, false)));
            User submitted = admin(1L, !action.equals("disable"));
            if (action.equals("demote")) submitted.setRole(Role.STUDENT);
            assertThatThrownBy(() -> {
                if (action.equals("delete")) service.deleteUser(1L);
                else service.updateUser(1L, submitted);
            }).as(action).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("at least one active administrator");
            assertThat(current.isEnabled()).isTrue();
            assertThat(current.getRole()).isEqualTo(Role.ADMIN);
            verify(users, never()).save(any());
            verify(users, never()).deleteById(any());
        }
    }

    @Test
    void lastAdminCanEditContactDetailsWithoutLosingAccess() {
        UserRepository users = mock(UserRepository.class);
        User current = admin(1L, true);
        when(users.findById(1L)).thenReturn(Optional.of(current));
        when(users.findByRoleForUpdate(Role.ADMIN)).thenReturn(List.of(current));
        User submitted = admin(1L, true);
        submitted.setFullName("Updated Admin");
        new UserService(users, mock(PasswordEncoder.class)).updateUser(1L, submitted);
        assertThat(current.getFullName()).isEqualTo("Updated Admin");
        verify(users).save(current);
    }

    @Test
    void anotherActiveAdminAllowsDisableDemotionAndDeletion() {
        for (String action : List.of("disable", "demote", "delete")) {
            UserRepository users = mock(UserRepository.class);
            User current = admin(1L, true);
            when(users.findById(1L)).thenReturn(Optional.of(current));
            when(users.findByRoleForUpdate(Role.ADMIN)).thenReturn(List.of(current, admin(2L, true)));
            UserService service = new UserService(users, mock(PasswordEncoder.class));
            User submitted = admin(1L, !action.equals("disable"));
            if (action.equals("demote")) submitted.setRole(Role.DR);
            if (action.equals("delete")) {
                service.deleteUser(1L);
                verify(users).deleteById(1L);
            } else {
                service.updateUser(1L, submitted);
                verify(users).save(current);
            }
        }
    }
}