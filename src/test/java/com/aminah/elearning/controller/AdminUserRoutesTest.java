package com.aminah.elearning.controller;

import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.UserRepository;
import com.aminah.elearning.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminUserRoutesTest {
    @Test
    void legacyRoutesUseExistingAdminViewsAndRecoverInvalidEdits() {
        UserService users = mock(UserService.class);
        User user = new User(7L);
        user.setUsername("qa-admin");
        when(users.getUserById(7L)).thenReturn(user);
        UserController controller = new UserController(users);
        var model = new ExtendedModelMap();
        assertThat(controller.getAllUsers(model)).isEqualTo("redirect:/admin/users");
        assertThat(controller.viewUser(7L, model)).isEqualTo("admin/users/user-details");
        assertThat(controller.editUser(7L, model)).isEqualTo("admin/users/user-edit");
        doThrow(new IllegalArgumentException("Keep an active admin")).when(users).updateUser(7L, user);
        assertThat(controller.updateUser(7L, user, model)).isEqualTo("admin/users/user-edit");
        assertThat(model.get("error")).isEqualTo("Keep an active admin");
        assertThat(controller.enableUser(7L)).isEqualTo("redirect:/admin/users");
    }

    @Test
    void bothDeleteRoutesShowGuardFeedbackInsteadOfServerError() {
        UserService users = mock(UserService.class);
        doThrow(new IllegalArgumentException("Keep an active admin")).when(users).deleteUser(7L);
        var canonical = new AdminController(users, mock(CourseService.class), mock(UserRepository.class), mock(CourseEnrollmentService.class));
        var legacy = new UserController(users);
        var canonicalFlash = new RedirectAttributesModelMap();
        var legacyFlash = new RedirectAttributesModelMap();
        assertThat(canonical.deleteUser(7L, canonicalFlash)).isEqualTo("redirect:/admin/users");
        assertThat(legacy.deleteUser(7L, legacyFlash)).isEqualTo("redirect:/admin/users");
        assertThat(canonicalFlash.getFlashAttributes().get("error")).isEqualTo("Keep an active admin");
        assertThat(legacyFlash.getFlashAttributes().get("error")).isEqualTo("Keep an active admin");
    }
}