package com.aditya.nexora.userService.service;


import io.jsonwebtoken.JwtBuilder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.util.Map;


public interface JwtService {

    String generateAccessToken(UserDetails userDetails);
    String generateRefreshToken(UserDetails userDetails);
    String getUsernameFromToken(String token);

    public boolean validateAccessToken(String token, UserDetails userDetails);
    public boolean validateRefreshToken(String token, UserDetails userDetails);

    public String buildToken(Map<String, Object> claims, String subject, SecretKey secret, long expirationMs);

    public String extractEmailFromAccessToken(String token);
    public String extractEmailFromRefreshToken(String token);

    public String extractUsernameFromToken(String token);

    public boolean isTokenExpired(String token, SecretKey secret);

}
