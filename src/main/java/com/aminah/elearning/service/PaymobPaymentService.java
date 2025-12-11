package com.aminah.elearning.service;

import com.aminah.elearning.model.CourseEnrollment;

import com.aminah.elearning.repository.CourseEnrollmentRepository;
import com.aminah.elearning.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
@RequiredArgsConstructor
public class PaymobPaymentService {

    @Value("${paymob.api.key}")
    private String paymobApiKey;

    @Value("${paymob.integration.id}")
    private String integrationId;

    @Value("${paymob.iframe.id}")
    private String iframeId;

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

    public Integer createOrder(String token, Double amount) {

        Map<String, Object> body = new HashMap<>();
        body.put("auth_token", token);
        body.put("delivery_needed", "false");
        body.put("amount_cents", (int)(amount * 100));
        body.put("currency", "EGP");
        body.put("items", List.of());

        Map resp = rest.postForObject(orderUrl, body, Map.class);

        return (Integer) resp.get("id");
    }

    public String generatePaymentKey(String token, Integer orderId,
                                     Double amount, String studentEmail) {

        Map<String, Object> billing = Map.of(
                "first_name", "Student",
                "last_name", "User",
                "email", studentEmail,
                "phone_number", "NA",
                "country", "EG",
                "city", "Cairo"
        );

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
