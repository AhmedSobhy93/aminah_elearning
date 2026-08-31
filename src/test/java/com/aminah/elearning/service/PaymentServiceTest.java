package com.aminah.elearning.service;

import com.aminah.elearning.model.Course;
import com.aminah.elearning.model.CourseEnrollment;
import com.aminah.elearning.model.Payment;
import com.aminah.elearning.model.User;
import com.aminah.elearning.repository.PaymentRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceTest {

    @Test
    void retryKeepsPendingGatewayOrderForTheSameAmount() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        CourseEnrollmentService enrollmentService = mock(CourseEnrollmentService.class);
        PaymentService service = new PaymentService(paymentRepository, enrollmentService, mock(EmailService.class));
        User user = new User(7L);
        Course course = new Course(12L);
        course.setPrice(150.0);
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setId(55L);
        enrollment.setCourse(course);
        Payment existing = new Payment();
        existing.setStatus("PENDING");
        existing.setGateway("PAYMOB");
        existing.setGatewayOrderId("12345");
        existing.setAmount(150.0);
        when(enrollmentService.findForUpdate(55L)).thenReturn(enrollment);
        when(paymentRepository.findByCourseEnrollment(enrollment)).thenReturn(Optional.of(existing));
        when(paymentRepository.save(existing)).thenReturn(existing);

        Payment retried = service.createPayment(user, enrollment, "PAYMOB");

        assertThat(retried.getGatewayOrderId()).isEqualTo("12345");
    }

    @Test
    void refundRevokesPreviouslySuccessfulEnrollment() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        CourseEnrollmentService enrollmentService = mock(CourseEnrollmentService.class);
        PaymentService service = new PaymentService(paymentRepository, enrollmentService, mock(EmailService.class));
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setId(55L);
        enrollment.setPaymentStatus("SUCCESS");
        Payment payment = new Payment();
        payment.setStatus("SUCCESS");
        payment.setCourseEnrollment(enrollment);
        when(paymentRepository.findForUpdateByGatewayOrder("PAYMOB", "12345"))
                .thenReturn(Optional.of(payment));

        Payment refunded = service.failGatewayPayment("PAYMOB", "12345", "REFUNDED");

        assertThat(refunded.getStatus()).isEqualTo("REFUNDED");
        verify(paymentRepository).save(payment);
        verify(enrollmentService).markPaymentStatus(55L, "REFUNDED");
    }

    @Test
    void replayedSuccessCannotReactivateRefundedPayment() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        CourseEnrollmentService enrollmentService = mock(CourseEnrollmentService.class);
        PaymentService service = new PaymentService(paymentRepository, enrollmentService, mock(EmailService.class));
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setId(55L);
        enrollment.setPaymentStatus("REFUNDED");
        Payment payment = new Payment();
        payment.setStatus("REFUNDED");
        payment.setCourseEnrollment(enrollment);
        when(paymentRepository.findForUpdateByGatewayOrder("PAYMOB", "12345"))
                .thenReturn(Optional.of(payment));

        Payment result = service.completeGatewayPayment("PAYMOB", "12345");

        assertThat(result.getStatus()).isEqualTo("REFUNDED");
        verify(paymentRepository, never()).save(payment);
        verify(enrollmentService, never()).markPaid(55L);
    }

    @Test
    void completesPendingGatewayPaymentAndEnrollmentOnce() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        CourseEnrollmentService enrollmentService = mock(CourseEnrollmentService.class);
        EmailService emailService = mock(EmailService.class);
        PaymentService service = new PaymentService(paymentRepository, enrollmentService, emailService);

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setId(55L);
        enrollment.setPaymentStatus("PENDING");
        Course course = new Course(12L);
        course.setTitle("Cardiology Basics");
        course.setPublished(true);
        enrollment.setCourse(course);
        User user = new User(7L);
        user.setEmail("student@example.com");

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setStatus("PENDING");
        payment.setAmount(150.0);
        payment.setCourseEnrollment(enrollment);

        when(paymentRepository.findForUpdateByGatewayOrder("PAYMOB", "12345"))
                .thenReturn(Optional.of(payment));

        Payment completed = service.completeGatewayPayment("PAYMOB", "12345");

        assertThat(completed.getStatus()).isEqualTo("SUCCESS");
        verify(paymentRepository).save(payment);
        verify(enrollmentService).markPaid(55L);
        verify(emailService).sendEmail(
                eq("student@example.com"),
                eq("Payment receipt - Aminah E-Learning"),
                argThat(body -> body.contains("Cardiology Basics") && body.contains("150.00 EGP"))
        );
    }

    @Test
    void replayedSuccessfulGatewayPaymentDoesNotWriteAgain() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        CourseEnrollmentService enrollmentService = mock(CourseEnrollmentService.class);
        EmailService emailService = mock(EmailService.class);
        PaymentService service = new PaymentService(paymentRepository, enrollmentService, emailService);

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setId(55L);
        enrollment.setPaymentStatus("SUCCESS");
        Course course = new Course(12L);
        course.setPublished(true);
        enrollment.setCourse(course);

        Payment payment = new Payment();
        payment.setStatus("SUCCESS");
        payment.setCourseEnrollment(enrollment);

        when(paymentRepository.findForUpdateByGatewayOrder("PAYMOB", "12345"))
                .thenReturn(Optional.of(payment));

        Payment completed = service.completeGatewayPayment("PAYMOB", "12345");

        assertThat(completed.getStatus()).isEqualTo("SUCCESS");
        verify(paymentRepository, never()).save(payment);
        verify(enrollmentService, never()).markPaid(55L);
        verify(emailService, never()).sendEmail(any(), any(), any());
    }
}
