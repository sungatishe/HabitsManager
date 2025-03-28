package com.example.habits.service.impl;

import com.example.habits.domain.User;
import com.example.habits.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

import java.util.Date;

import static java.util.Base64.getDecoder;

@Service
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret}")
    private String secretString;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;


    public JwtServiceImpl(@Value("${jwt.secret}") String secretString) {
        this.secretString = secretString;
        System.out.println("JwtService SECRET STRING (Base64): " + secretString);
        System.out.println("JwtService Decoded Key Length: " + getDecoder().decode(secretString).length);
        System.out.println("JwtService SecretKey: " + getSigningKey());
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretString);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(User user) {
        System.out.println("Generating Access Token with SecretKey: " + getSigningKey());
        String token = Jwts.builder()
                .claim("sub", user.getUsername())
                .claim("userId", user.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(getSigningKey())
                .compact();
        System.out.println("Generated Access Token: " + token);
        return token;
    }

    public String generateRefreshToken(User user) {
        System.out.println("Generating Refresh Token with SecretKey: " + getSigningKey());
        String token = Jwts.builder()
                .claim("sub", user.getUsername())
                .claim("userId", user.getId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .signWith(getSigningKey())
                .compact();
        System.out.println("Generated Refresh Token: " + token);
        return token;
    }

    public Claims extractClaims(String token) {
        System.out.println("Extracting claims with SecretKey: " + getSigningKey());
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

}
