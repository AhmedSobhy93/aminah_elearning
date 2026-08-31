package com.aminah.elearning.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RequestThrottleService {

    private static final int MAX_WINDOWS = 10_000;
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Window>> scopes = new ConcurrentHashMap<>();
    private final Clock clock;

    public RequestThrottleService() {
        this(Clock.systemUTC());
    }

    RequestThrottleService(Clock clock) {
        this.clock = clock;
    }

    public boolean allow(String scope, String identifier, int limit, Duration duration) {
        long now = clock.millis();
        ConcurrentHashMap<String, Window> windows = scopes.computeIfAbsent(scope, ignored -> new ConcurrentHashMap<>());
        String key = digest(normalize(identifier));
        if (!windows.containsKey(key) && windows.size() >= MAX_WINDOWS) {
            windows.entrySet().removeIf(entry -> now >= entry.getValue().expiresAt());
            if (windows.size() >= MAX_WINDOWS) {
                return false;
            }
        }
        Window window = windows.compute(key, (ignored, existing) -> {
            if (existing == null || now >= existing.expiresAt()) {
                return new Window(now + duration.toMillis(), new AtomicInteger(1));
            }
            existing.count().incrementAndGet();
            return existing;
        });
        return window.count().get() <= limit;
    }

    private String normalize(String identifier) {
        return identifier == null ? "unknown" : identifier.trim().toLowerCase(Locale.ROOT);
    }

    private String digest(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Could not create throttle key", ex);
        }
    }

    private record Window(long expiresAt, AtomicInteger count) {
    }
}
