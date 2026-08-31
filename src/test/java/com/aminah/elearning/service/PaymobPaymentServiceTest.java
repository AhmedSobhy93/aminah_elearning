package com.aminah.elearning.service;

import com.aminah.elearning.model.Payment;
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

    @Test
    void normalizesAndAuthenticatesNestedWebhook() {
        PaymobPaymentService service = paymentServiceWithHmacSecret();
        Map<String, String> flat = callbackPayload();
        String hmac = hmacFor(flat);
        Map<String, Object> nested = new HashMap<>(flat);
        nested.remove("order");
        nested.remove("source_data.pan");
        nested.remove("source_data.sub_type");
        nested.remove("source_data.type");
        nested.put("order", Map.of("id", 654321));
        nested.put("source_data", Map.of("pan", "1234", "sub_type", "MasterCard", "type", "card"));

        Map<String, String> normalized = service.normalizeWebhook(Map.of("type", "TRANSACTION", "obj", nested), hmac);

        assertThat(normalized.get("order")).isEqualTo("654321");
        assertThat(service.isValidHmac(normalized)).isTrue();
    }

    @Test
    void requiresExactPaymentBinding() {
        PaymobPaymentService service = paymentServiceWithHmacSecret();
        Map<String, String> event = callbackPayload();
        Payment payment = new Payment();
        payment.setGatewayOrderId("654321");
        payment.setAmount(100.0);

        assertThat(service.matchesPayment(event, payment)).isTrue();
        event.put("amount_cents", "9999");
        assertThat(service.matchesPayment(event, payment)).isFalse();
    }

    @Test
    void rejectsWrongCurrencyIntegrationOrMerchant() {
        PaymobPaymentService service = paymentServiceWithHmacSecret();
        Payment payment = matchingPayment();

        Map<String, String> wrongCurrency = callbackPayload();
        wrongCurrency.put("currency", "USD");
        Map<String, String> wrongIntegration = callbackPayload();
        wrongIntegration.put("integration_id", "other");
        Map<String, String> wrongMerchant = callbackPayload();
        wrongMerchant.put("owner", "other");

        assertThat(service.matchesPayment(wrongCurrency, payment)).isFalse();
        assertThat(service.matchesPayment(wrongIntegration, payment)).isFalse();
        assertThat(service.matchesPayment(wrongMerchant, payment)).isFalse();
    }

    @Test
    void pendingEventDoesNotCompleteOrFailPayment() {
        PaymobPaymentService service = paymentServiceWithHmacSecret();
        Map<String, String> event = callbackPayload();
        event.put("pending", "true");

        assertThat(service.isSuccessful(event)).isFalse();
        assertThat(service.isTerminalFailure(event)).isFalse();
    }

    @Test
    void refundedOrVoidedEventIsTerminalFailure() {
        PaymobPaymentService service = paymentServiceWithHmacSecret();
        Map<String, String> refunded = callbackPayload();
        refunded.put("is_refunded", "true");
        Map<String, String> voided = callbackPayload();
        voided.put("is_voided", "true");

        assertThat(service.isSuccessful(refunded)).isFalse();
        assertThat(service.isTerminalFailure(refunded)).isTrue();
        assertThat(service.failureStatus(refunded)).isEqualTo("REFUNDED");
        assertThat(service.isSuccessful(voided)).isFalse();
        assertThat(service.isTerminalFailure(voided)).isTrue();
        assertThat(service.failureStatus(voided)).isEqualTo("VOIDED");
    }

    private Payment matchingPayment() {
        Payment payment = new Payment();
        payment.setGatewayOrderId("654321");
        payment.setAmount(100.0);
        return payment;
    }

    private PaymobPaymentService paymentServiceWithHmacSecret() {
        PaymobPaymentService service = new PaymobPaymentService(mock(RestTemplate.class));
        ReflectionTestUtils.setField(service, "hmacSecret", HMAC_SECRET);
        ReflectionTestUtils.setField(service, "integrationId", "987654");
        ReflectionTestUtils.setField(service, "merchantId", "42");
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
