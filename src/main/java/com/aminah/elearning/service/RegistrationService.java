package com.aminah.elearning.service;

import com.aminah.elearning.model.User;
import com.aminah.elearning.model.VerificationToken;
import com.aminah.elearning.repository.UserRepository;
import com.aminah.elearning.repository.VerificationTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private EmailServiceSendGrid emailServiceSendGrid;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserRepository userRepository,
                               VerificationTokenRepository tokenRepository,
                               EmailServiceSendGrid emailServiceSendGrid,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailServiceSendGrid = emailServiceSendGrid;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(User user, String appUrl) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false);
        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepository.save(verificationToken);

        emailServiceSendGrid.sendEmail(user.getEmail(), token, appUrl);
    }

    public boolean confirmToken(String token) {
        VerificationToken vToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        if (vToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        Optional<User> optionalUser= userRepository.findById(vToken.getUser().getId());
        User user = optionalUser.orElseThrow(() -> new RuntimeException("User not found"));

        user.setEnabled(true);
        userRepository.save(user);
        return true;
    }
}
