package com.aminah.elearning.controller;

import com.aminah.elearning.model.Payment;
import com.aminah.elearning.service.CourseEnrollmentService;
import com.aminah.elearning.service.CourseService;
import com.aminah.elearning.service.PaymentService;
import com.aminah.elearning.service.PaymobPaymentService;
import com.aminah.elearning.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentControllerTest {

    @Test
    void browserCallbackNeverCompletesPayment() {
        PaymentService payments = mock(PaymentService.class);
        PaymobPaymentService paymob = mock(PaymobPaymentService.class);
        PaymentController controller = controller(payments, paymob);
        Map<String, String> callback = Map.of("hmac", "valid", "success", "true", "pending", "false");
        when(paymob.isValidHmac(callback)).thenReturn(true);
        when(paymob.isSuccessful(callback)).thenReturn(true);

        String redirect = controller.callback(callback);

        assertThat(redirect).isEqualTo("redirect:/student/my-courses?payment=processing");
        verify(payments, never()).completeGatewayPayment("PAYMOB", callback.get("order"));
    }

    @Test
    void validBoundWebhookCompletesPayment() {
        PaymentService payments = mock(PaymentService.class);
        PaymobPaymentService paymob = mock(PaymobPaymentService.class);
        PaymentController controller = controller(payments, paymob);
        Map<String, Object> body = Map.of("type", "TRANSACTION");
        Map<String, String> event = new HashMap<>();
        event.put("hmac", "valid");
        event.put("order", "123");
        Payment payment = new Payment();
        when(paymob.normalizeWebhook(body, "valid")).thenReturn(event);
        when(paymob.isValidHmac(event)).thenReturn(true);
        when(payments.findByGatewayOrder("PAYMOB", "123")).thenReturn(payment);
        when(paymob.matchesPayment(event, payment)).thenReturn(true);
        when(paymob.isSuccessful(event)).thenReturn(true);

        ResponseEntity<String> response = controller.webhook("valid", body);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(payments).completeGatewayPayment("PAYMOB", "123");
    }

    @Test
    void mismatchedWebhookCannotCompletePayment() {
        PaymentService payments = mock(PaymentService.class);
        PaymobPaymentService paymob = mock(PaymobPaymentService.class);
        PaymentController controller = controller(payments, paymob);
        Map<String, Object> body = Map.of("type", "TRANSACTION");
        Map<String, String> event = Map.of("hmac", "valid", "order", "123");
        Payment payment = new Payment();
        when(paymob.normalizeWebhook(body, "valid")).thenReturn(event);
        when(paymob.isValidHmac(event)).thenReturn(true);
        when(payments.findByGatewayOrder("PAYMOB", "123")).thenReturn(payment);
        when(paymob.matchesPayment(event, payment)).thenReturn(false);

        ResponseEntity<String> response = controller.webhook("valid", body);

        assertThat(response.getStatusCode().value()).isEqualTo(422);
        verify(payments, never()).completeGatewayPayment("PAYMOB", "123");
    }

    @Test
    void invalidWebhookSignatureIsRejected() {
        PaymentService payments = mock(PaymentService.class);
        PaymobPaymentService paymob = mock(PaymobPaymentService.class);
        PaymentController controller = controller(payments, paymob);
        Map<String, Object> body = Map.of("type", "TRANSACTION");
        Map<String, String> event = Map.of("hmac", "invalid", "order", "123");
        when(paymob.normalizeWebhook(body, "invalid")).thenReturn(event);
        when(paymob.isValidHmac(event)).thenReturn(false);

        ResponseEntity<String> response = controller.webhook("invalid", body);

        assertThat(response.getStatusCode().value()).isEqualTo(403);
        verify(payments, never()).findByGatewayOrder("PAYMOB", "123");
        verify(payments, never()).completeGatewayPayment("PAYMOB", "123");
    }

    @Test
    void terminalFailureMarksPaymentFailedWithoutUnlockingCourse() {
        PaymentService payments = mock(PaymentService.class);
        PaymobPaymentService paymob = mock(PaymobPaymentService.class);
        PaymentController controller = controller(payments, paymob);
        Map<String, Object> body = Map.of("type", "TRANSACTION");
        Map<String, String> event = Map.of("hmac", "valid", "order", "123");
        Payment payment = new Payment();
        when(paymob.normalizeWebhook(body, "valid")).thenReturn(event);
        when(paymob.isValidHmac(event)).thenReturn(true);
        when(payments.findByGatewayOrder("PAYMOB", "123")).thenReturn(payment);
        when(paymob.matchesPayment(event, payment)).thenReturn(true);
        when(paymob.isSuccessful(event)).thenReturn(false);
        when(paymob.isTerminalFailure(event)).thenReturn(true);
        when(paymob.failureStatus(event)).thenReturn("FAILED");

        ResponseEntity<String> response = controller.webhook("valid", body);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(payments).failGatewayPayment("PAYMOB", "123", "FAILED");
        verify(payments, never()).completeGatewayPayment("PAYMOB", "123");
    }

    private PaymentController controller(PaymentService payments, PaymobPaymentService paymob) {
        return new PaymentController(
                mock(CourseService.class),
                mock(CourseEnrollmentService.class),
                payments,
                paymob,
                mock(UserService.class)
        );
    }
}
