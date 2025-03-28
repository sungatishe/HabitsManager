package com.example.habits.service;

import com.example.habits.domain.User;
import com.example.habits.dto.auth.LoginRequestDTO;
import com.example.habits.dto.auth.RegisterRequestDTO;
import com.example.habits.dto.auth.TokenResponseDTO;

public interface AuthenticationService {
    User registerUser(RegisterRequestDTO request);
    TokenResponseDTO loginUser(LoginRequestDTO request);
    TokenResponseDTO refreshAccessToken(String refreshToken);
}
