package com.aminah.elearning.controller;

import com.aminah.elearning.model.PasswordResetToken;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.PasswordResetTokenRepository;
import com.aminah.elearning.repository.UserRepository;
import com.aminah.elearning.service.EmailService;
import com.aminah.elearning.service.TokenService;
import com.aminah.elearning.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.Charset;

@Controller
public class PasswordController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PasswordResetTokenRepository resetTokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

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
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No user with this email"));

        PasswordResetToken token = tokenService.createPasswordResetToken(user);
        String resetLink = "http://localhost:8080/reset-password?token=" + token.getToken();
        emailService.sendVerificationEmail(user.getEmail(), "Password Reset", "Click: " + resetLink);

        model.addAttribute("message", "Reset link sent to your email.");
        return "profile/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "profile/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("token") String token,
                                @RequestParam("password") String password,
                                Model model) {
//        boolean success = userRepository.resetPassword(token, password);
//        model.addAttribute("success", success);
        PasswordResetToken resetTokenToken = resetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetTokenToken.isExpired()) {

            model.addAttribute("error", "Reset token expired.");
            return "profile/reset-password";
        }

        User user = resetTokenToken.getUser();
        user.setPassword(String.valueOf(passwordEncoder.encode(password)));
        userRepository.save(user);
        resetTokenRepository.delete(resetTokenToken);

        model.addAttribute("succuss", true);
        return "profile/reset-result";
    }
}
