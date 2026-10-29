package com.aminah.elearning.service;

import com.aminah.elearning.model.PasswordResetToken;
import com.aminah.elearning.model.User;
import com.aminah.elearning.model.VerificationToken;
import com.aminah.elearning.repository.PasswordResetTokenRepository;
import com.aminah.elearning.repository.VerificationTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TokenService {

    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final com.aminah.elearning.repository.UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public enum ResetResult { SUCCESS, INVALID, EXPIRED, PASSWORD_REQUIRED }

    public TokenService(
            VerificationTokenRepository verificationTokenRepository,
            PasswordResetTokenRepository resetTokenRepository,
            com.aminah.elearning.repository.UserRepository userRepository,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder
    ) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public VerificationToken createVerificationToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        VerificationToken token = new VerificationToken();
        token.setToken(tokenValue);
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusHours(24)); // 24h validity
        return verificationTokenRepository.save(token);
    }

    @Transactional
    public PasswordResetToken createPasswordResetToken(User user) {
        resetTokenRepository.deleteByUser(user);
        String tokenValue = UUID.randomUUID().toString();
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(tokenValue);
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(15)); // shorter for security
        return resetTokenRepository.save(token);
    }

    @Transactional
    public ResetResult resetPassword(String tokenValue, String password) {
        PasswordResetToken token = resetTokenRepository.findByTokenForUpdate(tokenValue).orElse(null);
        if (token == null) return ResetResult.INVALID;
        if (token.isExpired()) return ResetResult.EXPIRED;
        if (!org.springframework.util.StringUtils.hasText(password)) return ResetResult.PASSWORD_REQUIRED;

        if (userRepository.updatePasswordHash(token.getUser().getId(), passwordEncoder.encode(password)) != 1) {
            throw new IllegalStateException("Reset account no longer exists");
        }
        resetTokenRepository.delete(token);
        resetTokenRepository.flush();
        return ResetResult.SUCCESS;
    }
}
