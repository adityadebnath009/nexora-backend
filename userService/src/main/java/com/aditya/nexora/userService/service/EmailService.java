package com.aditya.nexora.userService.service;

public interface EmailService {
    void sendEmailVerification(String email, String token);
    void sendPasswordResetEmail(String email, String token);
}
