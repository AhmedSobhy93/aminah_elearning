package com.aminah.elearning.service;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RequestThrottleServiceTest {

    @Test
    void rejectsRequestsBeyondWindowLimit() {
        RequestThrottleService service = new RequestThrottleService();

        assertThat(service.allow("login", "student", 2, Duration.ofMinutes(1))).isTrue();
        assertThat(service.allow("login", "student", 2, Duration.ofMinutes(1))).isTrue();
        assertThat(service.allow("login", "student", 2, Duration.ofMinutes(1))).isFalse();
        assertThat(service.allow("login", "another-student", 2, Duration.ofMinutes(1))).isTrue();
    }

    @Test
    void capacityExhaustionInOneScopeDoesNotBlockLoginScopes() {
        RequestThrottleService service = new RequestThrottleService();
        for (int index = 0; index < 10_000; index++) {
            assertThat(service.allow("registration-email", "email-" + index, 1, Duration.ofMinutes(15))).isTrue();
        }

        assertThat(service.allow("registration-email", "overflow", 1, Duration.ofMinutes(15))).isFalse();
        assertThat(service.allow("login-ip", "203.0.113.20", 40, Duration.ofMinutes(10))).isTrue();
        assertThat(service.allow("login-account", "student", 10, Duration.ofMinutes(10))).isTrue();
    }
}
