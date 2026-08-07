package com.way2jobs.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // Change this key in production
    private static final String SECRET =
            "Way2JobsSecretKeyWay2JobsSecretKeyWay2Jobs2026";

    private final SecretKey secretKey =
            Keys.hmacShaKeyFor(SECRET.getBytes());

    private static final long EXPIRATION =
            1000 * 60 * 60 * 24; // 24 Hours

    public String generateToken(String email) {

        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

    }

    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaims(normalizeToken(token)).getSubject();
    }

    public String extractRole(String token) {
        return extractClaims(normalizeToken(token)).get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = extractClaims(normalizeToken(token));
            Date expiration = claims.getExpiration();
            return expiration == null || expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private String normalizeToken(String token) {
        String headerToken = extractTokenFromHeader(token);
        return headerToken != null ? headerToken : token;
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    public String extractTokenFromHeader(String header) {

    if (header != null && header.startsWith("Bearer ")) {
        return header.substring(7);
    }

    return null;
}

}