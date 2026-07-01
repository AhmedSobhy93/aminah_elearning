package com.aminah.elearning.config;

import com.aminah.elearning.model.Role;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.bootstrap-admin.enabled", havingValue = "true")
public class AdminBootstrapRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.username:}")
    private String username;

    @Value("${app.bootstrap-admin.email:}")
    private String email;

    @Value("${app.bootstrap-admin.password:}")
    private String password;

    @Value("${app.bootstrap-admin.full-name:Initial Administrator}")
    private String fullName;

    @Override
    public void run(String... args) {
        if (userRepository.countByRole(Role.ADMIN) > 0) {
            return;
        }

        validateConfiguredAdmin();
        failIfBootstrapIdentityAlreadyExists();

        User admin = new User();
        admin.setUsername(username.trim());
        admin.setEmail(email.trim());
        admin.setFullName(StringUtils.hasText(fullName) ? fullName.trim() : "Initial Administrator");
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);

        userRepository.save(admin);
        System.out.println("Initial admin account created from APP_BOOTSTRAP_ADMIN_* configuration.");
    }

    private void validateConfiguredAdmin() {
        if (!StringUtils.hasText(username)
                || !StringUtils.hasText(email)
                || !StringUtils.hasText(password)) {
            throw new IllegalStateException(
                    "APP_BOOTSTRAP_ADMIN_USERNAME, APP_BOOTSTRAP_ADMIN_EMAIL, and APP_BOOTSTRAP_ADMIN_PASSWORD must be set when APP_BOOTSTRAP_ADMIN_ENABLED=true"
            );
        }
        if (password.length() < 12) {
            throw new IllegalStateException("APP_BOOTSTRAP_ADMIN_PASSWORD must be at least 12 characters");
        }
    }

    private void failIfBootstrapIdentityAlreadyExists() {
        userRepository.findByUsername(username.trim()).ifPresent(user -> {
            throw new IllegalStateException("Bootstrap admin username already exists but no admin account is present");
        });
        userRepository.findByEmail(email.trim()).ifPresent(user -> {
            throw new IllegalStateException("Bootstrap admin email already exists but no admin account is present");
        });
    }
}
