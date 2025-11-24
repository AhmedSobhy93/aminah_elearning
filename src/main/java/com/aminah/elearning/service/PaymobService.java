package com.aminah.elearning.service;

import com.aminah.elearning.model.Payment;
import com.aminah.elearning.repository.PaymentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymobService {

    // In a real app, you’d use WebClient or RestTemplate to call Paymob APIs
    public String createPaymentRequest(Payment payment) {
        // Mock behavior for testing
        String courseId = payment.getCourseId();//getCourseEnrollmentId().getCourse().getId();
        return "/payments/mock-success/" + courseId;
    }

    public boolean handleWebhook(String payload, String signature) {
        // Validate and parse Paymob webhook
        // Example placeholder logic
        System.out.println("Received webhook: " + payload);
        return true;
    }
}