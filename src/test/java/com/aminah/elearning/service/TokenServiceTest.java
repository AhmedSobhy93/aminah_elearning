package com.aminah.elearning.service;

import com.aminah.elearning.model.PasswordResetToken;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.PasswordResetTokenRepository;
import com.aminah.elearning.repository.VerificationTokenRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TokenServiceTest {

    @Test
    void replacesPreviousResetTokenBeforeSavingNewOne() {
        VerificationTokenRepository verificationTokens = mock(VerificationTokenRepository.class);
        PasswordResetTokenRepository resetTokens = mock(PasswordResetTokenRepository.class);
        TokenService service = new TokenService(verificationTokens, resetTokens);
        User user = new User(4L);
        when(resetTokens.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PasswordResetToken created = service.createPasswordResetToken(user);

        var order = inOrder(resetTokens);
        order.verify(resetTokens).deleteByUser(user);
        order.verify(resetTokens).save(created);
        assertThat(created.getUser()).isSameAs(user);
    }
}
