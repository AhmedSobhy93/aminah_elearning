package com.aminah.elearning.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class ProductionEnvironmentValidatorTest {

    @Test
    void acceptsMinimalSafeProductionConfiguration() {
        ProductionEnvironmentValidator validator = new ProductionEnvironmentValidator(baseEnvironment());

        assertThat(validator.validate()).isEmpty();
    }

    @Test
    void rejectsMissingPublicAppUrlAndDatabase() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("app.url", "http://localhost:8080")
                .withProperty("spring.datasource.url", "${DATABASE_URL}")
                .withProperty("app.embedded-postgres.enabled", "true")
                .withProperty("app.seed.enabled", "true");

        ProductionEnvironmentValidator validator = new ProductionEnvironmentValidator(environment);

        assertThat(validator.validate()).containsExactly(
                "APP_URL must be an absolute HTTPS URL",
                "DATABASE_URL or JDBC_DATABASE_URL must be set",
                "EMBEDDED_POSTGRES_ENABLED must be false in production",
                "APP_SEED_ENABLED must be false in production"
        );
    }

    @Test
    void requiresSecretsOnlyForEnabledOptionalIntegrations() {
        MockEnvironment environment = baseEnvironment()
                .withProperty("app.email.enabled", "true")
                .withProperty("app.email.provider", "sendgrid")
                .withProperty("aws.enabled", "true")
                .withProperty("paymob.enabled", "true");

        ProductionEnvironmentValidator validator = new ProductionEnvironmentValidator(environment);

        assertThat(validator.validate()).containsExactly(
                "APP_EMAIL_FROM must be set when APP_EMAIL_ENABLED=true",
                "SENDGRID_API_KEY must be set when SendGrid email is enabled",
                "AWS_S3_BUCKET must be set when AWS_ENABLED=true",
                "PAYMOB_API_KEY must be set when PAYMOB_ENABLED=true",
                "PAYMOB_INTEGRATION_ID must be set when PAYMOB_ENABLED=true",
                "PAYMOB_MERCHANT_ID must be set when PAYMOB_ENABLED=true",
                "PAYMOB_HMAC_SECRET must be set when PAYMOB_ENABLED=true",
                "PAYMOB_IFRAME_ID must be set when PAYMOB_ENABLED=true"
        );
    }

    private MockEnvironment baseEnvironment() {
        return new MockEnvironment()
                .withProperty("app.url", "https://aminah.example.com")
                .withProperty("spring.datasource.url", "jdbc:postgresql://db.example.com:5432/aminah")
                .withProperty("app.embedded-postgres.enabled", "false")
                .withProperty("app.seed.enabled", "false")
                .withProperty("app.email.enabled", "false")
                .withProperty("aws.enabled", "false")
                .withProperty("paymob.enabled", "false");
    }
}
