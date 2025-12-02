package com.aminah.elearning.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceSendGrid {

    @Value("${spring.sendgrid.api-key}")
    private String sendGridApiKey;

    public String sendEmail(String to, String subject, String body) {
        Email from = new Email("test@aminah.com");  // must be verified sender
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
