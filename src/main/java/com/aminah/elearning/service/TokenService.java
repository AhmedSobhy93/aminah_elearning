package com.aminah.elearning.service;

import com.aminah.elearning.model.PasswordResetToken;
import com.aminah.elearning.model.User;
import com.aminah.elearning.model.VerificationToken;
import com.aminah.elearning.repository.PasswordResetTokenRepository;
import com.aminah.elearning.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TokenService {

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository resetTokenRepository;

    public VerificationToken createVerificationToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        VerificationToken token = new VerificationToken();
        token.setToken(tokenValue);
        token.setUserId(user.getId());
        token.setExpiryDate(LocalDateTime.now().plusHours(24)); // 24h validity
        return verificationTokenRepository.save(token);
    }

    public PasswordResetToken createPasswordResetToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(tokenValue);
        token.setUserId(user.getId());
        token.setExpiryDate(LocalDateTime.now().plusMinutes(15)); // shorter for security
        return resetTokenRepository.save(token);
    }
}
