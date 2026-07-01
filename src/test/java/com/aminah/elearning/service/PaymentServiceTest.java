package com.aminah.elearning.service;

import com.aminah.elearning.model.CourseEnrollment;
import com.aminah.elearning.model.Payment;
import com.aminah.elearning.repository.PaymentRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceTest {

    @Test
    void completesPendingGatewayPaymentAndEnrollmentOnce() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        CourseEnrollmentService enrollmentService = mock(CourseEnrollmentService.class);
        PaymentService service = new PaymentService(paymentRepository, enrollmentService);

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setId(55L);
        enrollment.setPaymentStatus("PENDING");

        Payment payment = new Payment();
        payment.setStatus("PENDING");
        payment.setCourseEnrollment(enrollment);

        when(paymentRepository.findByGatewayAndGatewayOrderId("PAYMOB", "12345"))
                .thenReturn(Optional.of(payment));

        Payment completed = service.completeGatewayPayment("PAYMOB", "12345");

        assertThat(completed.getStatus()).isEqualTo("SUCCESS");
        verify(paymentRepository).save(payment);
        verify(enrollmentService).markPaid(55L);
    }

    @Test
    void replayedSuccessfulGatewayPaymentDoesNotWriteAgain() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        CourseEnrollmentService enrollmentService = mock(CourseEnrollmentService.class);
        PaymentService service = new PaymentService(paymentRepository, enrollmentService);

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setId(55L);
        enrollment.setPaymentStatus("SUCCESS");

        Payment payment = new Payment();
        payment.setStatus("SUCCESS");
        payment.setCourseEnrollment(enrollment);

        when(paymentRepository.findByGatewayAndGatewayOrderId("PAYMOB", "12345"))
                .thenReturn(Optional.of(payment));

        Payment completed = service.completeGatewayPayment("PAYMOB", "12345");

        assertThat(completed.getStatus()).isEqualTo("SUCCESS");
        verify(paymentRepository, never()).save(payment);
        verify(enrollmentService, never()).markPaid(55L);
    }
}
