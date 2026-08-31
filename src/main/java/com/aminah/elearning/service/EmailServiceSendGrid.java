package com.aminah.elearning.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmailServiceSendGrid implements EmailService {

    private final boolean emailEnabled;
    private final String provider;
    private final String sendGridApiKey;
    private final String fromEmail;

    public EmailServiceSendGrid(
            @Value("${app.email.enabled:false}") boolean emailEnabled,
            @Value("${app.email.provider:sendgrid}") String provider,
            @Value("${spring.sendgrid.api-key:}") String sendGridApiKey,
            @Value("${app.email.from:}") String fromEmail
    ) {
        this.emailEnabled = emailEnabled;
        this.provider = provider;
        this.sendGridApiKey = sendGridApiKey;
        this.fromEmail = fromEmail;
    }

    @Override
    public EmailResult sendEmail(String to, String subject, String body) {
        String providerName = "sendgrid";
        if (!emailEnabled) {
            return EmailResult.skipped(providerName, "Email disabled");
        }
        if (!"sendgrid".equalsIgnoreCase(provider)) {
            return EmailResult.skipped(providerName, "Email provider is not SendGrid");
        }
        if (!StringUtils.hasText(sendGridApiKey) || !StringUtils.hasText(fromEmail)) {
            return EmailResult.skipped(providerName, "SendGrid email is not configured");
        }
        if (!StringUtils.hasText(to)) {
            return EmailResult.skipped(providerName, "Recipient email is missing");
        }

        Email from = new Email(fromEmail);
        Email toEmail = new Email(to);
        Content content = new Content("text/html", body);
        Mail mail = new Mail(from, subject, toEmail, content);

        SendGrid sg = new SendGrid(sendGridApiKey);

        try {
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            boolean success = response.getStatusCode() >= 200 && response.getStatusCode() < 300;
            if (success) {
                return EmailResult.sent(providerName, "Accepted by SendGrid", response.getStatusCode());
            }
            return EmailResult.failed(providerName, "SendGrid returned non-success status", response.getStatusCode());
        } catch (Exception e) {
            return EmailResult.failed(providerName, "Error sending email: " + e.getMessage(), null);
        }
    }
}
