package com.tunitx.PRM.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiry.hours}")
    private int expiryHours;

    // this gives cryptographic hash of the secret- cannot be reverse engineering to get the secret
    private SecretKey getSigningKey(){
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(Long userId, String username, String role){
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + (long) expiryHours * 60 * 60 * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token){
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token){
        return extractAllClaims(token).get("role", String.class);
    }

    public Long extractUserId(String token){
        return extractAllClaims(token).get("userId", Long.class);
    }

    public boolean isTokenValid(String token){
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().after(new Date());
        }
        catch (Exception e){
            return false;
        }
    }
}
