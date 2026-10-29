package com.aminah.elearning.controller;

import com.aminah.elearning.model.User;
import com.aminah.elearning.model.PasswordResetToken;
import com.aminah.elearning.repository.UserRepository;
import com.aminah.elearning.service.EmailService;
import com.aminah.elearning.service.TokenService;
import com.aminah.elearning.service.RequestThrottleService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
public class PasswordController {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final RequestThrottleService throttle;
    private final String appUrl;

    public PasswordController(
            UserRepository userRepository,
            TokenService tokenService,
            EmailService emailService,
            RequestThrottleService throttle,
            @Value("${app.url}") String appUrl
    ) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.throttle = throttle;
        this.appUrl = appUrl;
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "profile/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @RequestParam("email") String email,
            HttpServletRequest request,
            Model model
    ) {
        String normalizedEmail = email.trim().toLowerCase();
        if (throttle.allow("password-reset", request.getRemoteAddr() + "|" + normalizedEmail, 3, java.time.Duration.ofMinutes(15))) {
            userRepository.findByEmail(normalizedEmail).ifPresent(user -> {
                PasswordResetToken token = tokenService.createPasswordResetToken(user);
                String resetLink = appUrl + "/reset-password?token=" + token.getToken();
                emailService.sendEmail(user.getEmail(), "Password Reset", "Click: " + resetLink);
            });
        }

        model.addAttribute("message", "If an account exists for this email, a reset link has been sent.");
        return "profile/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "profile/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("token") String token, @RequestParam("password") String password, Model model) {
        model.addAttribute("token", token);
        TokenService.ResetResult result = tokenService.resetPassword(token, password);
        if (result == TokenService.ResetResult.SUCCESS) {
            model.addAttribute("success", true);
            return "profile/reset-result";
        }
        String error = switch (result) {
            case INVALID -> "This reset link is invalid or has already been used. Request a new link.";
            case EXPIRED -> "Reset token expired.";
            case PASSWORD_REQUIRED -> "A nonblank password is required.";
            default -> throw new IllegalStateException("Unexpected reset result");
        };
        model.addAttribute("error", error);
        return "profile/reset-password";
    }
}
