package com.aminah.elearning.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailServiceSendGridTest {

    @Test
    void returnsStructuredSkippedResultWhenEmailIsDisabled() {
        EmailServiceSendGrid service = new EmailServiceSendGrid(false, "sendgrid", "api-key", "from@example.com");

        EmailResult result = service.sendEmail("student@example.com", "Subject", "Body");

        assertThat(result.sent()).isFalse();
        assertThat(result.provider()).isEqualTo("sendgrid");
        assertThat(result.message()).isEqualTo("Email disabled");
        assertThat(result.statusCode()).isNull();
    }

    @Test
    void returnsStructuredSkippedResultWhenSendGridIsNotConfigured() {
        EmailServiceSendGrid service = new EmailServiceSendGrid(true, "sendgrid", "", "");

        EmailResult result = service.sendEmail("student@example.com", "Subject", "Body");

        assertThat(result.sent()).isFalse();
        assertThat(result.provider()).isEqualTo("sendgrid");
        assertThat(result.message()).isEqualTo("SendGrid email is not configured");
        assertThat(result.statusCode()).isNull();
    }

    @Test
    void returnsStructuredSkippedResultWhenRecipientIsMissing() {
        EmailServiceSendGrid service = new EmailServiceSendGrid(true, "sendgrid", "api-key", "from@example.com");

        EmailResult result = service.sendEmail(" ", "Subject", "Body");

        assertThat(result.sent()).isFalse();
        assertThat(result.provider()).isEqualTo("sendgrid");
        assertThat(result.message()).isEqualTo("Recipient email is missing");
        assertThat(result.statusCode()).isNull();
    }
}
