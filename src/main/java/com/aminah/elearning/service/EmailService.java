package com.aminah.elearning.service;

public interface EmailService {

    EmailResult sendEmail(String to, String subject, String body);
}
