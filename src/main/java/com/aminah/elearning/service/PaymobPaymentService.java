package com.aminah.elearning.service;

import com.aminah.elearning.model.Payment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PaymobPaymentService {

    private static final List<String> HMAC_KEYS = List.of(
            "amount_cents", "created_at", "currency", "error_occured",
            "has_parent_transaction", "id", "integration_id", "is_3d_secure",
            "is_auth", "is_capture", "is_refunded", "is_standalone_payment",
            "is_voided", "order", "owner", "pending", "source_data.pan",
            "source_data.sub_type", "source_data.type", "success"
    );

    @Value("${paymob.enabled:false}")
    private boolean enabled;
    @Value("${paymob.api.key:}")
    private String paymobApiKey;
    @Value("${paymob.integration.id:}")
    private String integrationId;
    @Value("${paymob.merchant.id:}")
    private String merchantId;
    @Value("${paymob.iframe.id:}")
    private String iframeId;
    @Value("${paymob.hmac:}")
    private String hmacSecret;
    @Value("${paymob.url.auth}")
    private String authUrl;
    @Value("${paymob.url.order}")
    private String orderUrl;
    @Value("${paymob.url.paymentKey}")
    private String paymentKeyUrl;

    private final RestTemplate rest;

    public PaymobPaymentService(RestTemplate rest) {
        this.rest = rest;
    }

    public String getAuthToken() {
        Map<?, ?> response = rest.postForObject(authUrl, Map.of("api_key", paymobApiKey), Map.class);
        if (response == null || response.get("token") == null) {
            throw new IllegalStateException("Paymob did not return an authentication token");
        }
        return response.get("token").toString();
    }

    public Integer createOrder(String token, Double amount, Long merchantOrderId) {
        Map<String, Object> body = new HashMap<>();
        body.put("auth_token", token);
        body.put("delivery_needed", "false");
        body.put("amount_cents", amountToCents(amount));
        body.put("currency", "EGP");
        body.put("items", List.of());
        body.put("merchant_order_id", merchantOrderId.toString());

        Map<?, ?> response = rest.postForObject(orderUrl, body, Map.class);
        if (response == null || !(response.get("id") instanceof Number id)) {
            throw new IllegalStateException("Paymob did not return an order id");
        }
        return id.intValue();
    }

    public String generatePaymentKey(String token, Integer orderId, Double amount, String studentEmail) {
        Map<String, Object> billing = new HashMap<>();
        billing.put("first_name", "Student");
        billing.put("last_name", "User");
        billing.put("email", studentEmail);
        billing.put("phone_number", "NA");
        billing.put("country", "EG");
        billing.put("city", "Cairo");
        billing.put("street", "NA");
        billing.put("building", "NA");
        billing.put("floor", "NA");
        billing.put("apartment", "NA");
        billing.put("postal_code", "NA");
        billing.put("shipping_method", "NA");
        billing.put("state", "NA");

        Map<String, Object> body = new HashMap<>();
        body.put("auth_token", token);
        body.put("amount_cents", amountToCents(amount));
        body.put("expiration", 3600);
        body.put("order_id", orderId);
        body.put("billing_data", billing);
        body.put("currency", "EGP");
        body.put("integration_id", integrationId);
        body.put("lock_order_when_paid", true);

        Map<?, ?> response = rest.postForObject(paymentKeyUrl, body, Map.class);
        if (response == null || response.get("token") == null) {
            throw new IllegalStateException("Paymob did not return a payment key");
        }
        return response.get("token").toString();
    }

    public String buildIframeUrl(String paymentKey) {
        return "https://accept.paymob.com/api/acceptance/iframes/" + iframeId + "?payment_token=" + paymentKey;
    }

    public boolean isConfigured() {
        return enabled
                && StringUtils.hasText(paymobApiKey)
                && StringUtils.hasText(integrationId)
                && StringUtils.hasText(merchantId)
                && StringUtils.hasText(iframeId)
                && StringUtils.hasText(hmacSecret);
    }

    public Map<String, String> normalizeWebhook(Map<String, Object> payload, String hmac) {
        Object candidate = payload.get("obj");
        Map<?, ?> transaction = candidate instanceof Map<?, ?> nested ? nested : payload;
        Map<String, String> normalized = new HashMap<>();
        for (String key : HMAC_KEYS) {
            normalized.put(key, readCanonicalValue(transaction, key));
        }
        normalized.put("hmac", hmac == null ? "" : hmac);
        return normalized;
    }

    public boolean isValidHmac(Map<String, String> request) {
        String received = request.get("hmac");
        if (!StringUtils.hasText(hmacSecret) || !StringUtils.hasText(received)) {
            return false;
        }
        String data = HMAC_KEYS.stream().map(key -> request.getOrDefault(key, "")).collect(Collectors.joining());
        try {
            byte[] expected = HexFormat.of().parseHex(hmacSha512(data));
            byte[] actual = HexFormat.of().parseHex(received.toLowerCase());
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    public boolean matchesPayment(Map<String, String> event, Payment payment) {
        if (payment == null || payment.getGatewayOrderId() == null || payment.getAmount() == null) {
            return false;
        }
        return payment.getGatewayOrderId().equals(event.get("order"))
                && Integer.toString(amountToCents(payment.getAmount())).equals(event.get("amount_cents"))
                && "EGP".equalsIgnoreCase(event.get("currency"))
                && integrationId.equals(event.get("integration_id"))
                && merchantId.equals(event.get("owner"));
    }

    public boolean isSuccessful(Map<String, String> event) {
        return isTrue(event, "success")
                && !isTrue(event, "pending")
                && !isTrue(event, "error_occured")
                && !isTrue(event, "is_refunded")
                && !isTrue(event, "is_voided");
    }

    public boolean isTerminalFailure(Map<String, String> event) {
        return !isTrue(event, "pending") && !isSuccessful(event);
    }

    public String failureStatus(Map<String, String> event) {
        if (isTrue(event, "is_refunded")) {
            return "REFUNDED";
        }
        if (isTrue(event, "is_voided")) {
            return "VOIDED";
        }
        return "FAILED";
    }

    int amountToCents(Double amount) {
        if (amount == null || !Double.isFinite(amount) || amount < 0) {
            throw new IllegalArgumentException("Invalid payment amount");
        }
        return BigDecimal.valueOf(amount).movePointRight(2).setScale(0, RoundingMode.HALF_UP).intValueExact();
    }

    private boolean isTrue(Map<String, String> event, String key) {
        return "true".equalsIgnoreCase(event.get(key));
    }

    private String readCanonicalValue(Map<?, ?> transaction, String key) {
        if (key.startsWith("source_data.")) {
            Object source = transaction.get("source_data");
            if (source instanceof Map<?, ?> sourceData) {
                return stringValue(sourceData.get(key.substring("source_data.".length())));
            }
        }
        Object value = transaction.get(key);
        if ("order".equals(key) && value instanceof Map<?, ?> order) {
            return stringValue(order.get("id"));
        }
        return stringValue(value);
    }

    private String stringValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private String hmacSha512(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Could not verify Paymob callback", e);
        }
    }
}
