package com.aditya.nexora.userService.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class EmailServiceImpl implements EmailService{


    @Override
    public void sendEmailVerification(String email, String token) {

    }

    @Override
    public void sendPasswordResetEmail(String email, String token) {

    }
}
