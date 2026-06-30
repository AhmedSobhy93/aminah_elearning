package com.aminah.elearning.controller;

import com.aminah.elearning.model.PasswordResetToken;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.PasswordResetTokenRepository;
import com.aminah.elearning.repository.UserRepository;
import com.aminah.elearning.service.EmailServiceSendGrid;
import com.aminah.elearning.service.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class PasswordController {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final EmailServiceSendGrid emailServiceSendGrid;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final String appUrl;

    public PasswordController(
            UserRepository userRepository,
            TokenService tokenService,
            EmailServiceSendGrid emailServiceSendGrid,
            PasswordResetTokenRepository resetTokenRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.url}") String appUrl
    ) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.emailServiceSendGrid = emailServiceSendGrid;
        this.resetTokenRepository = resetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.appUrl = appUrl;
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "profile/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
//        try {
//            userService.createPasswordResetToken(email);
//            model.addAttribute("message", "Password reset link sent to your email.");
//        } catch (Exception e) {
//            model.addAttribute("error", "Email not found.");
//        }
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("No user with this email"));

        PasswordResetToken token = tokenService.createPasswordResetToken(user);
        String resetLink = appUrl + "/reset-password?token=" + token.getToken();
        emailServiceSendGrid.sendEmail(user.getEmail(), "Password Reset", "Click: " + resetLink);

        model.addAttribute("message", "Reset link sent to your email.");
        return "profile/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "profile/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("token") String token, @RequestParam("password") String password, Model model) {
//        boolean success = userRepository.resetPassword(token, password);
//        model.addAttribute("success", success);
        PasswordResetToken resetTokenToken = resetTokenRepository.findByToken(token).orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetTokenToken.isExpired()) {

            model.addAttribute("error", "Reset token expired.");
            return "profile/reset-password";
        }

        Optional<User> optionalUser = userRepository.findById(resetTokenToken.getUser().getId());
        User user = optionalUser.orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        resetTokenRepository.delete(resetTokenToken);

        model.addAttribute("succuss", true);
        return "profile/reset-result";
    }
}
