package com.aminah.elearning.service;

import com.aminah.elearning.model.PasswordResetToken;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {
    @Test
    void invalidExpiredAndBlankRequestsCannotChangePasswordOrConsumeToken() {
        for (String scenario : new String[]{"invalid", "expired", "blank"}) {
            PasswordResetTokenRepository tokens = mock(PasswordResetTokenRepository.class);
            UserRepository users = mock(UserRepository.class);
            PasswordEncoder encoder = mock(PasswordEncoder.class);
            TokenService service = new TokenService(mock(VerificationTokenRepository.class), tokens, users, encoder);
            PasswordResetToken token = new PasswordResetToken();
            token.setExpiryDate(LocalDateTime.now().plusMinutes(scenario.equals("expired") ? -1 : 15));
            when(tokens.findByTokenForUpdate("qa-token")).thenReturn(scenario.equals("invalid") ? Optional.empty() : Optional.of(token));
            TokenService.ResetResult expected = scenario.equals("invalid") ? TokenService.ResetResult.INVALID : scenario.equals("expired") ? TokenService.ResetResult.EXPIRED : TokenService.ResetResult.PASSWORD_REQUIRED;
            assertThat(service.resetPassword("qa-token", scenario.equals("blank") ? " \t " : "new-password")).isEqualTo(expected);
            verifyNoInteractions(users, encoder);
            verify(tokens, never()).delete(any(PasswordResetToken.class));
        }
    }

    @Test
    void successfulResetUpdatesOnlyPasswordAndConsumesTokenAfterSavingHash() {
        PasswordResetTokenRepository tokens = mock(PasswordResetTokenRepository.class);
        UserRepository users = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        TokenService service = new TokenService(mock(VerificationTokenRepository.class), tokens, users, encoder);
        User user = new User(7L);
        user.setEnabled(false);
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        when(tokens.findByTokenForUpdate("qa-token")).thenReturn(Optional.of(token));
        when(users.updatePasswordHash(7L, "new-hash")).thenReturn(1);
        when(encoder.encode("new-password")).thenReturn("new-hash");
        assertThat(service.resetPassword("qa-token", "new-password")).isEqualTo(TokenService.ResetResult.SUCCESS);
        assertThat(user.isEnabled()).isFalse();
        var order = inOrder(users, tokens);
        order.verify(users).updatePasswordHash(7L, "new-hash");
        order.verify(tokens).delete(token);
        order.verify(tokens).flush();
    }
}