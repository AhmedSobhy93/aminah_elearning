package com.aminah.elearning.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmailServiceSendGrid {

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

    public String sendEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            return "Email disabled";
        }
        if (!"sendgrid".equalsIgnoreCase(provider)) {
            return "Email provider is not SendGrid";
        }
        if (!StringUtils.hasText(sendGridApiKey) || !StringUtils.hasText(fromEmail)) {
            return "SendGrid email is not configured";
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
            return "Status: " + response.getStatusCode() +
                    " | Body: " + response.getBody();
        } catch (Exception e) {
            return "Error sending email: " + e.getMessage();
        }
    }
}
