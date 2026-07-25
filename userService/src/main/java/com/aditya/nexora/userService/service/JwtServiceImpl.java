package com.aditya.nexora.userService.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;


@Service
public class JwtServiceImpl implements JwtService{

    private final SecretKey accessSecretKey;
    private final SecretKey refreshSecretKey;
    private final long accessExpirationTime;
    private final long refreshExpirationTime;


    public JwtServiceImpl(@Value("${JWT_ACCESS_SECRET}")String accessSecretKey, @Value("${JWT_REFRESH_SECRET}") String refreshSecretKey, @Value("${JWT_ACCESS_EXPIRATION_MS}")long accessExpirationTime, @Value("${JWT_REFRESH_EXPIRATION_MS}") long refreshExpirationTime) {
        this.accessSecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecretKey));;
        this.refreshSecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecretKey));;
        this.accessExpirationTime = accessExpirationTime;
        this.refreshExpirationTime = refreshExpirationTime;
    }

    @Override
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = Map.of("email", userDetails.getUsername(), "roles", userDetails.getAuthorities());

        return buildToken(claims, userDetails.getUsername(), accessSecretKey, accessExpirationTime);
    }

    @Override
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(Map.of("email", userDetails.getUsername()), userDetails.getUsername(), refreshSecretKey, refreshExpirationTime);
    }

    @Override
    public String getUsernameFromToken(String token) {
        return "";
    }

    @Override
    public boolean validateAccessToken(String token, UserDetails userDetails) {
        String email = extractEmailFromAccessToken(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token, accessSecretKey);
    }

    @Override
    public boolean validateRefreshToken(String token, UserDetails userDetails) {
        String email = extractEmailFromRefreshToken(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token, refreshSecretKey);
    }

    @Override
    public String buildToken(Map<String, Object> claims, String subject, SecretKey secret, long expirationMs) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + expirationMs);

        return Jwts.builder().
                claims(claims).
                subject(subject).
                signWith(secret).
                expiration(validity).
                compact();


    }

    @Override
    public String extractEmailFromAccessToken(String token) {
        return extractAllClaims(token, accessSecretKey).getSubject();
    }

    @Override
    public String extractEmailFromRefreshToken(String token) {
        return extractAllClaims(token, refreshSecretKey).getSubject();
    }

    @Override
    public String extractUsernameFromToken(String token) {
        return "";
    }

    @Override
    public boolean isTokenExpired(String token, SecretKey secret)
    {
        Claims claim = extractAllClaims(token, secret);
        Date expiration = claim.getExpiration();
        return expiration.before(new Date());
    }



    private Claims extractAllClaims(String token, SecretKey secret){
        return Jwts.parser()
                .verifyWith(secret)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
