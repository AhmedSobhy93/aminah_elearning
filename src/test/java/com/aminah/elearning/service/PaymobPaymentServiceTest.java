package com.aminah.elearning.service;

import com.aminah.elearning.repository.CourseEnrollmentRepository;
import com.aminah.elearning.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PaymobPaymentServiceTest {

    private static final String HMAC_SECRET = "phase-10-hmac-secret";
    private static final String[] PAYMOB_HMAC_KEYS = {
            "amount_cents",
            "created_at",
            "currency",
            "error_occured",
            "has_parent_transaction",
            "id",
            "integration_id",
            "is_3d_secure",
            "is_auth",
            "is_capture",
            "is_refunded",
            "is_standalone_payment",
            "is_voided",
            "order",
            "owner",
            "pending",
            "source_data.pan",
            "source_data.sub_type",
            "source_data.type",
            "success"
    };

    @Test
    void acceptsValidCallbackHmac() {
        PaymobPaymentService service = paymentServiceWithHmacSecret();
        Map<String, String> callback = callbackPayload();
        callback.put("hmac", hmacFor(callback));

        assertThat(service.isValidHmac(callback)).isTrue();
    }

    @Test
    void rejectsMissingOrTamperedCallbackHmac() {
        PaymobPaymentService service = paymentServiceWithHmacSecret();
        Map<String, String> callback = callbackPayload();
        callback.put("hmac", hmacFor(callback));
        callback.put("success", "false");

        assertThat(service.isValidHmac(callback)).isFalse();
        assertThat(service.isValidHmac(callbackPayload())).isFalse();
    }

    private PaymobPaymentService paymentServiceWithHmacSecret() {
        PaymobPaymentService service = new PaymobPaymentService(
                mock(RestTemplate.class),
                mock(PaymentRepository.class),
                mock(CourseEnrollmentRepository.class)
        );
        ReflectionTestUtils.setField(service, "hmacSecret", HMAC_SECRET);
        return service;
    }

    private Map<String, String> callbackPayload() {
        Map<String, String> payload = new HashMap<>();
        payload.put("amount_cents", "10000");
        payload.put("created_at", "2026-06-30T12:00:00");
        payload.put("currency", "EGP");
        payload.put("error_occured", "false");
        payload.put("has_parent_transaction", "false");
        payload.put("id", "123456");
        payload.put("integration_id", "987654");
        payload.put("is_3d_secure", "true");
        payload.put("is_auth", "false");
        payload.put("is_capture", "false");
        payload.put("is_refunded", "false");
        payload.put("is_standalone_payment", "true");
        payload.put("is_voided", "false");
        payload.put("order", "654321");
        payload.put("owner", "42");
        payload.put("pending", "false");
        payload.put("source_data.pan", "1234");
        payload.put("source_data.sub_type", "MasterCard");
        payload.put("source_data.type", "card");
        payload.put("success", "true");
        return payload;
    }

    private String hmacFor(Map<String, String> payload) {
        String data = Stream.of(PAYMOB_HMAC_KEYS)
                .map(key -> payload.getOrDefault(key, ""))
                .collect(Collectors.joining());
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(HMAC_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Could not create test HMAC", e);
        }
    }
}
