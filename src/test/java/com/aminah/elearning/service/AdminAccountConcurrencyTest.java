package com.aminah.elearning.service;

import com.aminah.elearning.config.EmbeddedPostgresConfig;
import com.aminah.elearning.model.Role;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.List;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.flyway.enabled=false", "spring.jpa.show-sql=false"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({EmbeddedPostgresConfig.class, UserService.class, AdminAccountConcurrencyTest.PasswordConfig.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class AdminAccountConcurrencyTest {
    @Autowired UserRepository users;
    @Autowired UserService service;
    @Autowired PlatformTransactionManager transactions;

    @TestConfiguration
    static class PasswordConfig {
        @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    }

    private User create(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.test");
        user.setFullName("Disposable concurrency admin");
        user.setPassword("unused-test-hash");
        user.setRole(Role.ADMIN);
        user.setEnabled(true);
        return users.saveAndFlush(user);
    }

    @Test
    void accountMutationsWaitForAdminLockAndCannotRemoveTheRemainingAdmin() throws Exception {
        User first = create("lock-qa-first");
        User second = create("lock-qa-second");
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> holder = executor.submit(() -> new TransactionTemplate(transactions).executeWithoutResult(status -> {
                users.findByRoleForUpdate(Role.ADMIN);
                locked.countDown();
                try { if (!release.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Lock test timed out"); }
                catch (InterruptedException ex) { Thread.currentThread().interrupt(); throw new IllegalStateException(ex); }
            }));
            assertThat(locked.await(10, TimeUnit.SECONDS)).isTrue();
            Future<?> deletion = executor.submit(() -> service.deleteUser(first.getId()));
            assertThatThrownBy(() -> deletion.get(300, TimeUnit.MILLISECONDS)).isInstanceOf(TimeoutException.class);
            release.countDown();
            holder.get(10, TimeUnit.SECONDS);
            deletion.get(10, TimeUnit.SECONDS);
            assertThatThrownBy(() -> service.deleteUser(second.getId())).isInstanceOf(IllegalArgumentException.class);
            assertThat(users.findById(second.getId())).get().extracting(User::isEnabled).isEqualTo(true);
        } finally {
            release.countDown();
            executor.shutdown();
            executor.awaitTermination(15, TimeUnit.SECONDS);
            users.deleteAllById(List.of(first.getId(), second.getId()));
        }
    }
}