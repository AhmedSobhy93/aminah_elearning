package com.aminah.elearning.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class CloudDatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = environment.getProperty("DATABASE_URL");
        if (!StringUtils.hasText(databaseUrl)) {
            return;
        }

        Map<String, Object> properties = new HashMap<>();
        if (databaseUrl.startsWith("jdbc:postgresql://")) {
            properties.put("spring.datasource.url", databaseUrl);
        } else if (databaseUrl.startsWith("postgres://") || databaseUrl.startsWith("postgresql://")) {
            addPostgresProperties(databaseUrl, environment, properties);
        }

        if (!properties.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource("cloudDatabaseUrl", properties));
        }
    }

    private void addPostgresProperties(
            String databaseUrl,
            ConfigurableEnvironment environment,
            Map<String, Object> properties
    ) {
        URI uri = URI.create(databaseUrl);
        String path = uri.getPath();
        String databaseName = path != null && path.length() > 1 ? path.substring(1) : "";

        StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://")
                .append(uri.getHost());
        if (uri.getPort() > 0) {
            jdbcUrl.append(":").append(uri.getPort());
        }
        jdbcUrl.append("/").append(databaseName);
        if (StringUtils.hasText(uri.getQuery())) {
            jdbcUrl.append("?").append(uri.getQuery());
        }
        properties.put("spring.datasource.url", jdbcUrl.toString());

        if (!StringUtils.hasText(environment.getProperty("DATABASE_USERNAME"))
                && StringUtils.hasText(uri.getUserInfo())) {
            String[] userInfo = uri.getUserInfo().split(":", 2);
            properties.put("spring.datasource.username", decode(userInfo[0]));
            if (userInfo.length > 1 && !StringUtils.hasText(environment.getProperty("DATABASE_PASSWORD"))) {
                properties.put("spring.datasource.password", decode(userInfo[1]));
            }
        }
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
