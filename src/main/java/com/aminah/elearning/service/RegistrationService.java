package com.aminah.elearning.service;

import com.aminah.elearning.exception.DuplicateUserException;
import com.aminah.elearning.model.Role;
import com.aminah.elearning.model.User;
import com.aminah.elearning.model.VerificationToken;
import com.aminah.elearning.repository.UserRepository;
import com.aminah.elearning.repository.VerificationTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserRepository userRepository,
                               VerificationTokenRepository tokenRepository,
                               EmailService emailService,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(User user, String appUrl) {
        normalizeRegistration(user);
        validateUniqueRegistration(user);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(false);
        user.setRole(Role.STUDENT);
        userRepository.save(user);

        sendConfirmationEmail(user, appUrl);
    }

    @Transactional
    public void resendConfirmation(String email, String appUrl) {
        if (!StringUtils.hasText(email)) {
            return;
        }

        userRepository.findByEmail(email.trim().toLowerCase())
                .filter(user -> !user.isEnabled())
                .ifPresent(user -> {
                    tokenRepository.deleteByUser(user);
                    sendConfirmationEmail(user, appUrl);
                });
    }

    private void sendConfirmationEmail(User user, String appUrl) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepository.save(verificationToken);

        String confirmationLink = appUrl + "/profile/confirm?token=" + token;
        emailService.sendEmail(
                user.getEmail(),
                "Confirm your registration - Aminah E-Learning",
                "Click the link to activate your account: <a href=\"" + confirmationLink + "\">Confirm account</a>"
        );
    }

    private void normalizeRegistration(User user) {
        if (StringUtils.hasText(user.getUsername())) {
            user.setUsername(user.getUsername().trim());
        }
        if (StringUtils.hasText(user.getEmail())) {
            user.setEmail(user.getEmail().trim().toLowerCase());
        }
        if (StringUtils.hasText(user.getFullName())) {
            user.setFullName(user.getFullName().trim());
        }
        if (StringUtils.hasText(user.getPhoneNumber())) {
            user.setPhoneNumber(user.getPhoneNumber().trim());
        }
    }

    private void validateUniqueRegistration(User user) {
        if (!StringUtils.hasText(user.getUsername())) {
            throw new IllegalArgumentException("Username is required");
        }
        if (!StringUtils.hasText(user.getEmail())) {
            throw new IllegalArgumentException("Email is required");
        }
        userRepository.findByUsername(user.getUsername()).ifPresent(existing -> {
            throw new DuplicateUserException("Username is already registered");
        });
        userRepository.findByEmail(user.getEmail()).ifPresent(existing -> {
            throw new DuplicateUserException("Email is already registered");
        });
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
