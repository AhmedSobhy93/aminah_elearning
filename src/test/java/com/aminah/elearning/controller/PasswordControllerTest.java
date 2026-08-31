package com.aminah.elearning.controller;

import com.aminah.elearning.model.PasswordResetToken;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.PasswordResetTokenRepository;
import com.aminah.elearning.repository.UserRepository;
import com.aminah.elearning.service.EmailService;
import com.aminah.elearning.service.TokenService;
import com.aminah.elearning.service.RequestThrottleService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasswordControllerTest {

    @Test
    void forgotPasswordDoesNotRevealUnknownEmail() {
        UserRepository userRepository = mock(UserRepository.class);
        TokenService tokenService = mock(TokenService.class);
        EmailService emailService = mock(EmailService.class);
        PasswordController controller = controller(userRepository, tokenService, emailService);
        Model model = new ConcurrentModel();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        String view = controller.processForgotPassword(" Missing@Example.COM ", request, model);

        assertThat(view).isEqualTo("profile/forgot-password");
        assertThat(model.getAttribute("message"))
                .isEqualTo("If an account exists for this email, a reset link has been sent.");
        verify(tokenService, never()).createPasswordResetToken(any());
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void forgotPasswordSendsResetWhenEmailExists() {
        UserRepository userRepository = mock(UserRepository.class);
        TokenService tokenService = mock(TokenService.class);
        EmailService emailService = mock(EmailService.class);
        PasswordController controller = controller(userRepository, tokenService, emailService);
        Model model = new ConcurrentModel();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        User user = new User();
        user.setEmail("student@example.com");
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("reset-token");

        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(user));
        when(tokenService.createPasswordResetToken(user)).thenReturn(token);

        String view = controller.processForgotPassword("Student@Example.COM", request, model);

        assertThat(view).isEqualTo("profile/forgot-password");
        assertThat(model.getAttribute("message"))
                .isEqualTo("If an account exists for this email, a reset link has been sent.");
        verify(emailService).sendEmail(
                "student@example.com",
                "Password Reset",
                "Click: https://aminah.example.com/reset-password?token=reset-token"
        );
    }

    private PasswordController controller(
            UserRepository userRepository,
            TokenService tokenService,
            EmailService emailService
    ) {
        RequestThrottleService throttle = mock(RequestThrottleService.class);
        when(throttle.allow(any(), any(), anyInt(), any())).thenReturn(true);
        return new PasswordController(
                userRepository,
                tokenService,
                emailService,
                mock(PasswordResetTokenRepository.class),
                mock(PasswordEncoder.class),
                throttle,
                "https://aminah.example.com"
        );
    }
}
