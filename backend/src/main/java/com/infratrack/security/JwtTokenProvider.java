package com.infratrack.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Component
public class JwtTokenProvider {
    @Value("${jwt.secret:MySuperSecretKeyForInfraTrackLocalDevOnlyChangeMe123456789}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private int jwtExpirationMs;

    private SecretKey signingKey;

    @PostConstruct
    void initializeSigningKey() {
        this.signingKey = buildSigningKey(jwtSecret);
    }

    private static SecretKey buildSigningKey(String secret) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hashed = digest.digest(secretBytes);
            return Keys.hmacShaKeyFor(hashed);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Unable to hash JWT secret for HS512", exception);
        }
    }

    public String generateToken(Long userId, String email) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public Long getUserIdFromToken(String token) {
        return ((Number) Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("userId")).longValue();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }
}
