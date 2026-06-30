package com.aminah.elearning.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceGmail {
    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailServiceGmail(
            JavaMailSender mailSender,
            @Value("${spring.mail.from:}") String fromEmail
    ) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public void sendVerificationEmail(String email, String token, String appUrl) {
        String confirmationUrl = appUrl + "/profile/confirm?token=" + token;
        String subject = "Confirm your registration - Aminah E-Learning";
        String message = "Click the link to activate your account:\n" + confirmationUrl;

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(fromEmail);
        mail.setTo(email);
        mail.setSubject(subject);
        mail.setText(message);
        mailSender.send(mail);
    }
}
