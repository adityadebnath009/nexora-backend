package com.aditya.nexora.userService.repository;

import com.aditya.nexora.userService.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    public Optional<EmailVerificationToken> findByToken(String token);
}
