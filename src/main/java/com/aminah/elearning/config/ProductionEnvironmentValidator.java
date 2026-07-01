package com.aminah.elearning.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("production")
@RequiredArgsConstructor
public class ProductionEnvironmentValidator implements ApplicationRunner {

    private final Environment environment;

    @Override
    public void run(ApplicationArguments args) {
        List<String> errors = validate();
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Invalid production configuration: " + String.join("; ", errors));
        }
    }

    List<String> validate() {
        List<String> errors = new ArrayList<>();

        requirePublicAppUrl(errors);
        requireManagedPostgres(errors);
        requireDisabledDevData(errors);
        requireEnabledIntegrationSecrets(errors);

        return errors;
    }

    private void requirePublicAppUrl(List<String> errors) {
        String appUrl = property("app.url");
        if (!StringUtils.hasText(appUrl)) {
            errors.add("APP_URL must be set");
            return;
        }

        URI uri;
        try {
            uri = URI.create(appUrl.trim());
        } catch (IllegalArgumentException ex) {
            errors.add("APP_URL must be a valid absolute URL");
            return;
        }

        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (!("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme)) || !StringUtils.hasText(host)) {
            errors.add("APP_URL must be an absolute http(s) URL");
            return;
        }

        if ("localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host)) {
            errors.add("APP_URL must not point to localhost in production");
        }
    }

    private void requireManagedPostgres(List<String> errors) {
        String datasourceUrl = property("spring.datasource.url");
        if (!StringUtils.hasText(datasourceUrl) || datasourceUrl.contains("${")) {
            errors.add("DATABASE_URL or JDBC_DATABASE_URL must be set");
            return;
        }

        if (!datasourceUrl.startsWith("jdbc:postgresql://")) {
            errors.add("spring.datasource.url must use PostgreSQL JDBC in production");
        }
    }

    private void requireDisabledDevData(List<String> errors) {
        if (environment.getProperty("app.embedded-postgres.enabled", Boolean.class, false)) {
            errors.add("EMBEDDED_POSTGRES_ENABLED must be false in production");
        }
        if (environment.getProperty("app.seed.enabled", Boolean.class, false)) {
            errors.add("APP_SEED_ENABLED must be false in production");
        }
    }

    private void requireEnabledIntegrationSecrets(List<String> errors) {
        if (environment.getProperty("app.email.enabled", Boolean.class, false)) {
            require(errors, "app.email.from", "APP_EMAIL_FROM must be set when APP_EMAIL_ENABLED=true");
            if ("sendgrid".equalsIgnoreCase(property("app.email.provider", "sendgrid"))) {
                require(errors, "sendgrid.api-key", "SENDGRID_API_KEY must be set when SendGrid email is enabled");
            }
        }

        if (environment.getProperty("aws.enabled", Boolean.class, false)) {
            require(errors, "aws.s3.bucket", "AWS_S3_BUCKET must be set when AWS_ENABLED=true");
        }
    }

    private void require(List<String> errors, String property, String message) {
        if (!StringUtils.hasText(property(property))) {
            errors.add(message);
        }
    }

    private String property(String name) {
        return property(name, null);
    }

    private String property(String name, String defaultValue) {
        try {
            return environment.getProperty(name, defaultValue);
        } catch (RuntimeException ex) {
            return defaultValue;
        }
    }
}
