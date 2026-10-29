package com.aminah.elearning.service;

import com.aminah.elearning.config.EmbeddedPostgresConfig;
import com.aminah.elearning.model.*;
import com.aminah.elearning.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.flyway.enabled=false", "spring.jpa.show-sql=false"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({EmbeddedPostgresConfig.class, TokenService.class, PasswordResetTransactionTest.PasswordConfig.class})
@org.springframework.test.annotation.DirtiesContext(classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PasswordResetTransactionTest {
    @Autowired UserRepository users;
    @SpyBean PasswordResetTokenRepository tokens;
    @Autowired TokenService service;
    @SpyBean PasswordEncoder encoder;
    @Autowired org.springframework.transaction.PlatformTransactionManager transactions;
    private Long userId;
    private Long tokenId;
    private String tokenValue;

    @TestConfiguration
    static class PasswordConfig {
        @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    }

    @BeforeEach
    void fixture() {
        String suffix = UUID.randomUUID().toString();
        User user = new User();
        user.setUsername("local-reset-" + suffix);
        user.setEmail(suffix + "@example.test");
        user.setPassword("original-local-test-hash");
        user.setRole(Role.STUDENT);
        user.setEnabled(false);
        userId = users.saveAndFlush(user).getId();
        PasswordResetToken token = new PasswordResetToken();
        tokenValue = "local-token-" + suffix;
        token.setToken(tokenValue);
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        tokenId = tokens.saveAndFlush(token).getId();
    }

    @AfterEach
    void cleanup() {
        tokens.deleteAllById(List.of(tokenId));
        users.deleteById(userId);
    }

    @Test
    void resetPreservesConcurrentRoleAndStatusChange() {
        doAnswer(invocation -> {
            var update = new org.springframework.transaction.support.TransactionTemplate(transactions);
            update.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            update.executeWithoutResult(status -> {
                User user = users.findById(userId).orElseThrow();
                user.setRole(Role.DR);
                user.setEnabled(true);
                users.saveAndFlush(user);
            });
            return invocation.callRealMethod();
        }).when(encoder).encode("local-state-test-password");
        assertThat(service.resetPassword(tokenValue, "local-state-test-password")).isEqualTo(TokenService.ResetResult.SUCCESS);
        User user = users.findById(userId).orElseThrow();
        assertThat(user.getRole()).isEqualTo(Role.DR);
        assertThat(user.isEnabled()).isTrue();
        assertThat(encoder.matches("local-state-test-password", user.getPassword())).isTrue();
    }
    @Test
    void tokenDeletionFailureRollsBackAnAlreadyFlushedPasswordChange() {
        doThrow(new IllegalStateException("Simulated local deletion failure")).when(tokens).delete(any(PasswordResetToken.class));
        assertThatThrownBy(() -> service.resetPassword(tokenValue, "local-only-new-password")).isInstanceOf(IllegalStateException.class);
        assertThat(users.findById(userId)).get().extracting(User::getPassword).isEqualTo("original-local-test-hash");
        assertThat(tokens.findByToken(tokenValue)).isPresent();
    }

    @Test
    void concurrentReplayChangesPasswordExactlyOnceWithoutEnablingAccount() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<TokenService.ResetResult> first = executor.submit(() -> { start.await(); return service.resetPassword(tokenValue, "local-first-password"); });
            Future<TokenService.ResetResult> second = executor.submit(() -> { start.await(); return service.resetPassword(tokenValue, "local-second-password"); });
            start.countDown();
            TokenService.ResetResult firstResult = first.get(15, TimeUnit.SECONDS);
            TokenService.ResetResult secondResult = second.get(15, TimeUnit.SECONDS);
            assertThat(List.of(firstResult, secondResult)).containsExactlyInAnyOrder(TokenService.ResetResult.SUCCESS, TokenService.ResetResult.INVALID);
            User user = users.findById(userId).orElseThrow();
            assertThat(encoder.matches(firstResult == TokenService.ResetResult.SUCCESS ? "local-first-password" : "local-second-password", user.getPassword())).isTrue();
            assertThat(user.isEnabled()).isFalse();
            assertThat(tokens.findByToken(tokenValue)).isEmpty();
        } finally {
            executor.shutdown();
            executor.awaitTermination(15, TimeUnit.SECONDS);
        }
    }
}