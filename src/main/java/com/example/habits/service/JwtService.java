package com.example.habits.service;

import com.example.habits.domain.User;
import io.jsonwebtoken.Claims;

public interface JwtService {
    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    Claims extractClaims(String token);
    boolean isTokenValid(String token);
    boolean isTokenExpired(String token);
}
