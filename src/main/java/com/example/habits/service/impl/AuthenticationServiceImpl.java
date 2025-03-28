package com.example.habits.service.impl;

import com.example.habits.domain.RefreshToken;
import com.example.habits.domain.User;
import com.example.habits.dto.auth.LoginRequestDTO;
import com.example.habits.dto.auth.RegisterRequestDTO;
import com.example.habits.dto.auth.TokenResponseDTO;
import com.example.habits.repository.RefreshTokenRepository;
import com.example.habits.repository.UserRepository;

import com.example.habits.service.AuthenticationService;
import com.example.habits.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;


@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    public User registerUser(RegisterRequestDTO request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exist");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());

        return userRepository.save(user);
    }

    public TokenResponseDTO loginUser(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );


        if (authentication.isAuthenticated()) {
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            saveRefreshToken(user, refreshToken);

            return new TokenResponseDTO(accessToken, refreshToken);
        }
        throw new IllegalArgumentException("Invalid credentials");
    }

    public TokenResponseDTO refreshAccessToken(String refreshToken) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));

        // Проверяем валидность токена (подпись)
        if (!jwtService.isTokenValid(refreshToken)) {
            refreshTokenRepository.deleteByToken(refreshToken);
            throw new IllegalArgumentException("Invalid refresh token");
        }

        // Проверяем, не истек ли токен
        if (jwtService.isTokenExpired(refreshToken)) {
            refreshTokenRepository.deleteByToken(refreshToken);
            throw new IllegalArgumentException("Refresh token expired");
        }

        // Извлекаем username из refresh-токена
        String username = jwtService.extractClaims(refreshToken).getSubject();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Проверяем, совпадает ли пользователь токена с найденным пользователем
        if (!storedToken.getUser().getId().equals(user.getId())) {
            refreshTokenRepository.deleteByToken(refreshToken);
            throw new IllegalArgumentException("Refresh token does not belong to user");
        }

        // Генерируем новый access-токен
        String newAccessToken = jwtService.generateAccessToken(user);

        // Генерируем новый refresh-токен
        String newRefreshToken = jwtService.generateRefreshToken(user);

        // Удаляем старый refresh-токен из базы
        refreshTokenRepository.deleteByToken(refreshToken);

        // Сохраняем новый refresh-токен
        saveRefreshToken(user, newRefreshToken);

        // Возвращаем оба новых токена
        return new TokenResponseDTO(newAccessToken, newRefreshToken);
    }

    private void saveRefreshToken(User user, String refreshToken) {
        refreshTokenRepository.deleteByUserId(user.getId());
        RefreshToken token = new RefreshToken();
        token.setToken(refreshToken);
        token.setUser(user);
        token.setExpiryTime(Instant.now().plusMillis(jwtService.extractClaims(refreshToken).getExpiration().getTime() - System.currentTimeMillis()));
        refreshTokenRepository.save(token);
    }

}
