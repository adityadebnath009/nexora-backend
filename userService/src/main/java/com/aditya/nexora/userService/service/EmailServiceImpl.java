package com.aditya.nexora.userService.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;


@Slf4j
@Service
public class EmailServiceImpl implements EmailService{


    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;
    @Value("${spring.mail.password}")
    private String fromPassword;
    @Value("${spring.mail.host}")
    private String host;
    @Value("${spring.mail.port}")
    private int port;

    public EmailServiceImpl(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }
    private void sendEmail(String to, String subject, String template, Context context) throws MessagingException {
        try {


            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            String html = templateEngine.process(template, context);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Email sent to {} with subject {}", to, subject);
        }
        catch (MessagingException e) {
            log.error("Error sending email to {} with subject {}", to, subject, e);
            throw e;
        }



    }
    @Override
    public void sendEmailVerification(String email, String token) {
        log.info("Sending email verification to {}", email);

        String verificationUrl = "http://localhost:8080/api/v1/auth/verify-email?token=" + token;

        Context context = new Context();
        context.setVariable("verificationLink", verificationUrl);
        context.setVariable("email", email);
        String template = "email-verification";


        try{
            log.info("Sending email verification to {}", email);
            sendEmail(email,"Verify Your Nexora Account", "email/email-verification", context );
        } catch (MessagingException e) {
            log.error("Error sending email verification to {}", email, e);
            throw new MailException("Could not send verification email", e) {
                @Override
                public @Nullable Throwable getRootCause() {
                    return super.getRootCause();
                }
            };
        }


    }

    @Override
    public void sendPasswordResetEmail(String email, String token) {
        log.info("Preparing password reset email for {}", email);

        // We construct the reset URL
        String resetLink = "http://localhost:9010/auth/reset-password?token=" + token;

        // Set up Thymeleaf Context variables
        Context context = new Context();
        context.setVariable("email", email);
        context.setVariable("resetLink", resetLink);

        try {
            sendEmail(email, "Reset Your Nexora Password", "email/password-reset", context);
            log.info("Password reset email successfully sent to {}", email);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}", email, e);
            throw new MailException("Could not send password reset email", e) {
                @Override
                public @Nullable Throwable getRootCause() {
                    return super.getRootCause();
                }
            };
        }

    }
}
