package com.aminah.elearning.service;

import com.aminah.elearning.model.CourseEnrollment;
import com.aminah.elearning.model.Payment;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CourseEnrollmentService enrollmentService;
    private final EmailService emailService;

    @Transactional
    public Payment createPayment(User user, CourseEnrollment enrollment, String gateway) {
        if (enrollment == null || enrollment.getId() == null) {
            throw new IllegalArgumentException("A persisted enrollment is required before payment creation");
        }
        CourseEnrollment lockedEnrollment = enrollmentService.findForUpdate(enrollment.getId());
        Payment payment = paymentRepository.findByCourseEnrollment(lockedEnrollment).orElseGet(Payment::new);
        if ("SUCCESS".equalsIgnoreCase(payment.getStatus())) {
            return payment;
        }
        Double currentAmount = lockedEnrollment.getCourse().getPrice();
        boolean orderCanBeReused = "PENDING".equalsIgnoreCase(payment.getStatus())
                && gateway.equalsIgnoreCase(payment.getGateway())
                && payment.getAmount() != null
                && currentAmount != null
                && Double.compare(payment.getAmount(), currentAmount) == 0;
        if (!orderCanBeReused) {
            payment.setGatewayOrderId(null);
        }
        payment.setUser(user);
        payment.setCourseEnrollment(lockedEnrollment);
        payment.setAmount(currentAmount);
        payment.setStatus("PENDING");
        payment.setGateway(gateway);
        return paymentRepository.save(payment);
    }

    public Payment assignGatewayOrder(Payment payment, String gatewayOrderId) {
        payment.setGatewayOrderId(gatewayOrderId);
        payment.setStatus("PENDING");
        return paymentRepository.save(payment);
    }

    public void updatePaymentStatus(Payment payment, String status) {
        payment.setStatus(status);
        paymentRepository.save(payment);
    }

    // <-- Add this method
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id).orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    public Payment processPayment(User user, CourseEnrollment enrollment, Double amount, String gateway) {
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setCourseEnrollment(enrollment);
        payment.setAmount(amount);
        payment.setStatus("SUCCESS"); // Simulate success, integrate actual gateway
        payment.setGateway(gateway);
        paymentRepository.save(payment);

        // enroll after payment
//        enrollmentService.enrollUser(user, enrollment.getCourse());
        return payment;
    }

    public Payment findByGatewayOrder(String gateway, String gatewayOrderId) {
        return paymentRepository.findByGatewayAndGatewayOrderId(gateway, gatewayOrderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for gateway order"));
    }

    @Transactional
    public Payment completeGatewayPayment(String gateway, String gatewayOrderId) {
        Payment payment = paymentRepository.findForUpdateByGatewayOrder(gateway, gatewayOrderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for gateway order"));
        if ("REFUNDED".equalsIgnoreCase(payment.getStatus())
                || "VOIDED".equalsIgnoreCase(payment.getStatus())) {
            return payment;
        }
        boolean alreadySuccessful = "SUCCESS".equalsIgnoreCase(payment.getStatus());

        CourseEnrollment enrollment = payment.getCourseEnrollment();
        if (enrollment == null || enrollment.getCourse() == null || !enrollment.getCourse().isPublished()) {
            throw new IllegalStateException("Payment cannot unlock an unpublished course");
        }

        if (!alreadySuccessful) {
            payment.setStatus("SUCCESS");
            paymentRepository.save(payment);
        }

        if (enrollment != null && !"SUCCESS".equalsIgnoreCase(enrollment.getPaymentStatus())) {
            enrollmentService.markPaid(enrollment.getId());
        }

        if (!alreadySuccessful) {
            schedulePaymentReceipt(payment);
        }

        return payment;
    }

    @Transactional
    public Payment failGatewayPayment(String gateway, String gatewayOrderId) {
        return failGatewayPayment(gateway, gatewayOrderId, "FAILED");
    }

    @Transactional
    public Payment failGatewayPayment(String gateway, String gatewayOrderId, String failureStatus) {
        Payment payment = paymentRepository.findForUpdateByGatewayOrder(gateway, gatewayOrderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for gateway order"));
        boolean revocation = "REFUNDED".equalsIgnoreCase(failureStatus)
                || "VOIDED".equalsIgnoreCase(failureStatus);
        if (!"SUCCESS".equalsIgnoreCase(payment.getStatus()) || revocation) {
            payment.setStatus(failureStatus);
            paymentRepository.save(payment);
            CourseEnrollment enrollment = payment.getCourseEnrollment();
            if (enrollment != null && enrollment.getId() != null) {
                enrollmentService.markPaymentStatus(enrollment.getId(), failureStatus);
            }
        }
        return payment;
    }

    private void schedulePaymentReceipt(Payment payment) {
        PaymentReceipt receipt = paymentReceipt(payment);
        if (receipt == null) {
            return;
        }
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendPaymentReceipt(receipt);
                }
            });
        } else {
            sendPaymentReceipt(receipt);
        }
    }

    private PaymentReceipt paymentReceipt(Payment payment) {
        User user = payment.getUser();
        if (user == null || !StringUtils.hasText(user.getEmail())) {
            return null;
        }

        CourseEnrollment enrollment = payment.getCourseEnrollment();
        String courseName = "your course";
        if (enrollment != null && enrollment.getCourse() != null) {
            if (StringUtils.hasText(enrollment.getCourse().getTitle())) {
                courseName = enrollment.getCourse().getTitle();
            } else if (StringUtils.hasText(enrollment.getCourse().getCourseName())) {
                courseName = enrollment.getCourse().getCourseName();
            }
        }

        String amount = payment.getAmount() == null
                ? "the course fee"
                : String.format(Locale.US, "%.2f EGP", payment.getAmount());

        String body = "<p>Your payment for <strong>" + HtmlUtils.htmlEscape(courseName) + "</strong> was successful.</p>"
                + "<p>Amount: " + HtmlUtils.htmlEscape(amount) + "</p>"
                + "<p>Thank you for learning with Aminah E-Learning.</p>";

        return new PaymentReceipt(user.getEmail(), "Payment receipt - Aminah E-Learning", body);
    }

    private void sendPaymentReceipt(PaymentReceipt receipt) {
        emailService.sendEmail(receipt.to(), receipt.subject(), receipt.body());
    }

    private record PaymentReceipt(String to, String subject, String body) {
    }
}
