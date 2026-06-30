package com.aminah.elearning.service;

import com.aminah.elearning.model.CourseEnrollment;

import com.aminah.elearning.repository.CourseEnrollmentRepository;
import com.aminah.elearning.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
@Service
@RequiredArgsConstructor
public class PaymobPaymentService {

    @Value("${paymob.api.key}")
    private String paymobApiKey;

    @Value("${paymob.integration.id}")
    private String integrationId;

    @Value("${paymob.iframe.id}")
    private String iframeId;

    @Value("${paymob.hmac:}")
    private String hmacSecret;

    @Value("${paymob.url.auth}")
    private String authUrl;

    @Value("${paymob.url.order}")
    private String orderUrl;

    @Value("${paymob.url.paymentKey}")
    private String paymentKeyUrl;

    @Value("${paymob.url.callback}")
    private String callbackUrl;

    private final RestTemplate rest;
    private final PaymentRepository paymentRepo;
    private final CourseEnrollmentRepository enrollmentRepo;

    public String getAuthToken() {

        Map<String, Object> body = Map.of("api_key", paymobApiKey);

        Map response = rest.postForObject(authUrl, body, Map.class);

        return response.get("token").toString();
    }

    public Integer createOrder(String token, Double amount, Long merchantOrderId) {

        Map<String, Object> body = new HashMap<>();
        body.put("auth_token", token);
        body.put("delivery_needed", "false");
        body.put("amount_cents", (int)(amount * 100));
        body.put("currency", "EGP");
        body.put("items", List.of());
        body.put("merchant_order_id", merchantOrderId.toString());

        Map resp = rest.postForObject(orderUrl, body, Map.class);

        return (Integer) resp.get("id");
    }

    public String generatePaymentKey(String token, Integer orderId,
                                     Double amount, String studentEmail) {

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
        body.put("amount_cents", (int)(amount * 100));
        body.put("expiration", 3600);
        body.put("order_id", orderId);
        body.put("billing_data", billing);
        body.put("currency", "EGP");
        body.put("integration_id", integrationId);
        body.put("lock_order_when_paid", true);

        Map resp = rest.postForObject(paymentKeyUrl, body, Map.class);

        return resp.get("token").toString();
    }

    public String buildIframeUrl(String paymentKey) {
        return "https://accept.paymob.com/api/acceptance/iframes/"
                + iframeId + "?payment_token=" + paymentKey;
    }

    public boolean isConfigured() {
        return StringUtils.hasText(paymobApiKey)
                && StringUtils.hasText(integrationId)
                && StringUtils.hasText(iframeId);
    }

    public boolean isValidHmac(Map<String, String> req) {
        String received = req.get("hmac");
        if (!StringUtils.hasText(hmacSecret) || !StringUtils.hasText(received)) {
            return false;
        }

        String data = Stream.of(
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
                )
                .map(key -> req.getOrDefault(key, ""))
                .collect(Collectors.joining());

        return hmacSha512(data).equalsIgnoreCase(received);
    }

    private String hmacSha512(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Could not verify Paymob callback", e);
        }
    }

    public boolean handleCallback(Map<String, String> req) {

        String success = req.get("success");
        String enrollmentId = req.get("enrollment_id");

        if (!"true".equals(success))
            return false;

        CourseEnrollment e = enrollmentRepo.findById(Long.parseLong(enrollmentId))
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        e.setPaymentStatus("SUCCESS");
        enrollmentRepo.save(e);

        return true;
    }
}
