package com.example.habits.controller;

import com.example.habits.domain.User;
import com.example.habits.dto.auth.LoginRequestDTO;
import com.example.habits.dto.auth.RegisterRequestDTO;
import com.example.habits.dto.auth.TokenResponseDTO;
import com.example.habits.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequestDTO request) {
        User user = authenticationService.registerUser(request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        TokenResponseDTO response = authenticationService.loginUser(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refreshToken(@RequestParam String refreshToken) {
        TokenResponseDTO response = authenticationService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(response);
    }

}
