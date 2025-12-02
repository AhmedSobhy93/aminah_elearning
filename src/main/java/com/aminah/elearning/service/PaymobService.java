package com.aminah.elearning.service;

import com.aminah.elearning.model.Payment;

import org.springframework.stereotype.Service;


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