package com.aminah.elearning.service;

import com.aminah.elearning.model.CourseEnrollment;
import com.aminah.elearning.model.Payment;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CourseEnrollmentService enrollmentService;

    public Payment createPayment(User user, CourseEnrollment enrollment, String gateway) {
        Payment payment = new Payment();
        payment.setUserId(user.getId());
        payment.setCourseEnrollmentId(enrollment.getId());
//        payment.setAmount(enrollment.getPaymentStatus());
        payment.setStatus("PENDING");
        payment.setGateway(gateway);
        return paymentRepository.save(payment);
    }

    public void updatePaymentStatus(Payment payment, String status) {
        payment.setStatus(status);
        paymentRepository.save(payment);
    }

    // <-- Add this method
    public Payment getPaymentById(String id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }
    public Payment processPayment(User user, CourseEnrollment enrollment, Double amount, String gateway){
        Payment payment = new Payment();
        payment.setUserId(user.getId());
//        payment.setCourseEnrollmentId(enrollment);
        payment.setAmount(amount);
        payment.setStatus("SUCCESS"); // Simulate success, integrate actual gateway
        payment.setGateway(gateway);
        paymentRepository.save(payment);

        // enroll after payment
//        enrollmentService.enrollUser(user.getId(),user, enrollment.getCourseId());
        return payment;
    }
}
