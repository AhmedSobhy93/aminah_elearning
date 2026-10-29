package com.aminah.elearning.controller;

import com.aminah.elearning.model.PasswordResetToken;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.UserRepository;
import com.aminah.elearning.service.EmailService;
import com.aminah.elearning.service.TokenService;
import com.aminah.elearning.service.RequestThrottleService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PasswordControllerTest {
    private PasswordController controller(UserRepository users, TokenService tokens, EmailService email) {
        RequestThrottleService throttle = mock(RequestThrottleService.class);
        when(throttle.allow(any(), any(), anyInt(), any())).thenReturn(true);
        return new PasswordController(users, tokens, email, throttle, "https://aminah.example.com");
    }

    @Test
    void forgotPasswordDoesNotRevealUnknownEmail() {
        UserRepository users = mock(UserRepository.class);
        TokenService tokens = mock(TokenService.class);
        EmailService email = mock(EmailService.class);
        var model = new ConcurrentModel();
        var request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(users.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThat(controller(users, tokens, email).processForgotPassword(" Missing@Example.COM ", request, model)).isEqualTo("profile/forgot-password");
        assertThat(model.getAttribute("message")).isEqualTo("If an account exists for this email, a reset link has been sent.");
        verify(tokens, never()).createPasswordResetToken(any());
        verify(email, never()).sendEmail(any(), any(), any());
    }

    @Test
    void forgotPasswordSendsResetWhenEmailExists() {
        UserRepository users = mock(UserRepository.class);
        TokenService tokens = mock(TokenService.class);
        EmailService email = mock(EmailService.class);
        var request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        User user = new User();
        user.setEmail("student@example.com");
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("reset-token");
        when(users.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(tokens.createPasswordResetToken(user)).thenReturn(token);
        var model = new ConcurrentModel();
        assertThat(controller(users, tokens, email).processForgotPassword("Student@Example.COM", request, model)).isEqualTo("profile/forgot-password");
        assertThat(model.getAttribute("message")).isEqualTo("If an account exists for this email, a reset link has been sent.");
        verify(email).sendEmail("student@example.com", "Password Reset", "Click: https://aminah.example.com/reset-password?token=reset-token");
    }

    @Test
    void invalidExpiredAndBlankPasswordResultsKeepRecoveryTokenAndFeedback() {
        for (TokenService.ResetResult result : new TokenService.ResetResult[]{TokenService.ResetResult.INVALID, TokenService.ResetResult.EXPIRED, TokenService.ResetResult.PASSWORD_REQUIRED}) {
            TokenService tokens = mock(TokenService.class);
            when(tokens.resetPassword("qa-token", " ")).thenReturn(result);
            var model = new ConcurrentModel();
            assertThat(controller(mock(UserRepository.class), tokens, mock(EmailService.class)).resetPassword("qa-token", " ", model)).isEqualTo("profile/reset-password");
            assertThat(model.getAttribute("error")).isNotNull();
            assertThat(model.getAttribute("token")).isEqualTo("qa-token");
            assertThat(model.getAttribute("success")).isNull();
        }
    }

    @Test
    void successfulResetSuppliesTheResultTemplatesSuccessFlag() {
        TokenService tokens = mock(TokenService.class);
        when(tokens.resetPassword("qa-token", "new-password")).thenReturn(TokenService.ResetResult.SUCCESS);
        var model = new ConcurrentModel();
        assertThat(controller(mock(UserRepository.class), tokens, mock(EmailService.class)).resetPassword("qa-token", "new-password", model)).isEqualTo("profile/reset-result");
        assertThat(model.getAttribute("success")).isEqualTo(true);
    }
}