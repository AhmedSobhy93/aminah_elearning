package com.aminah.elearning.config;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class AwsS3ConfigTest {
    @Test
    void acceptsHttpsStorageOrigin() {
        assertThat(AwsS3Config.requireHttpsEndpoint("https://account.r2.cloudflarestorage.com").getHost())
                .isEqualTo("account.r2.cloudflarestorage.com");
    }

    @Test
    void rejectsInsecureOrCredentialBearingEndpoints() {
        for (String value : new String[]{"http://storage.example.com", "https://key:secret@storage.example.com", "https://storage.example.com/bucket", "https://storage.example.com?key=secret"}) {
            assertThatThrownBy(() -> AwsS3Config.requireHttpsEndpoint(value)).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
