package com.aminah.elearning.service;

public record EmailResult(
        boolean sent,
        String provider,
        String message,
        Integer statusCode
) {

    public static EmailResult sent(String provider, String message, Integer statusCode) {
        return new EmailResult(true, provider, message, statusCode);
    }

    public static EmailResult skipped(String provider, String message) {
        return new EmailResult(false, provider, message, null);
    }

    public static EmailResult failed(String provider, String message, Integer statusCode) {
        return new EmailResult(false, provider, message, statusCode);
    }
}
