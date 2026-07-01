package com.aminah.elearning.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class CloudDatabaseUrlEnvironmentPostProcessorTest {

    private final CloudDatabaseUrlEnvironmentPostProcessor postProcessor =
            new CloudDatabaseUrlEnvironmentPostProcessor();

    @Test
    void convertsRenderPostgresUrlToJdbcDatasourceProperties() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty(
                        "DATABASE_URL",
                        "postgres://user%40email:p%40ss@db.example.com:5432/aminah?sslmode=require"
                );

        postProcessor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://db.example.com:5432/aminah?sslmode=require");
        assertThat(environment.getProperty("spring.datasource.username"))
                .isEqualTo("user@email");
        assertThat(environment.getProperty("spring.datasource.password"))
                .isEqualTo("p@ss");
    }

    @Test
    void doesNotOverrideExplicitDatabaseCredentials() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("DATABASE_URL", "postgres://url-user:url-pass@db.example.com/aminah")
                .withProperty("DATABASE_USERNAME", "explicit-user")
                .withProperty("DATABASE_PASSWORD", "explicit-pass")
                .withProperty("spring.datasource.username", "explicit-user")
                .withProperty("spring.datasource.password", "explicit-pass");

        postProcessor.postProcessEnvironment(environment, null);

        assertThat(environment.getProperty("spring.datasource.url"))
                .isEqualTo("jdbc:postgresql://db.example.com/aminah");
        assertThat(environment.getProperty("spring.datasource.username"))
                .isEqualTo("explicit-user");
        assertThat(environment.getProperty("spring.datasource.password"))
                .isEqualTo("explicit-pass");
    }
}
