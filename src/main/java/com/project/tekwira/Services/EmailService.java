package com.project.tekwira.Services;

public interface EmailService {

    void sendEmail(
        String to,
        String subject,
        String body
    );
}
