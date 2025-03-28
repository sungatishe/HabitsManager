package com.example.habits.controller;

import com.example.habits.domain.User;
import com.example.habits.dto.auth.LoginRequestDTO;
import com.example.habits.dto.auth.RegisterRequestDTO;
import com.example.habits.dto.auth.TokenResponseDTO;
import com.example.habits.exception.GlobalExceptionHandler;
import com.example.habits.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private User user;
    private RegisterRequestDTO registerRequestDTO;
    private LoginRequestDTO loginRequestDTO;
    private TokenResponseDTO tokenResponseDTO;

    @BeforeEach
    void setUp() {
        // Настройка тестового пользователя
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setFullName("Test User");

        // Настройка DTO для регистрации
        registerRequestDTO = new RegisterRequestDTO();
        registerRequestDTO.setUsername("testuser");
        registerRequestDTO.setEmail("test@example.com");
        registerRequestDTO.setPassword("password");
        registerRequestDTO.setFullName("Test User");

        // Настройка DTO для логина
        loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setUsername("testuser");
        loginRequestDTO.setPassword("password");

        // Настройка DTO для токена
        tokenResponseDTO = new TokenResponseDTO("accessToken123", "refreshToken123");

        // Настройка MockMvc с глобальным обработчиком исключений
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // Тесты для register
    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        when(authenticationService.registerUser(any(RegisterRequestDTO.class))).thenReturn(user);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"email\":\"test@example.com\",\"password\":\"password\",\"fullName\":\"Test User\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"));
    }

    @Test
    void shouldReturnBadRequestWhenRegisterWithInvalidData() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ab\",\"email\":\"invalid-email\",\"password\":\"\",\"fullName\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("Username must be between 3 and 50 characters"))
                .andExpect(jsonPath("$.email").value("Email should be valid"))
                .andExpect(jsonPath("$.password").value("Password is required"))
                .andExpect(jsonPath("$.fullName").value("Full name is required"));
    }

    // Тесты для login
    @Test
    void shouldLoginUserSuccessfully() throws Exception {
        when(authenticationService.loginUser(any(LoginRequestDTO.class))).thenReturn(tokenResponseDTO);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testuser\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken123"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken123"));
    }

    @Test
    void shouldReturnBadRequestWhenLoginWithInvalidData() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("Username is required"))
                .andExpect(jsonPath("$.password").value("Password is required"));
    }

    // Тесты для refreshToken
    @Test
    void shouldRefreshTokenSuccessfully() throws Exception {
        when(authenticationService.refreshAccessToken("refreshToken123")).thenReturn(tokenResponseDTO);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .param("refreshToken", "refreshToken123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken123"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken123"));
    }

    @Test
    void shouldReturnBadRequestWhenRefreshTokenIsInvalid() throws Exception {
        when(authenticationService.refreshAccessToken("invalidToken"))
                .thenThrow(new IllegalArgumentException("Invalid refresh token"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .param("refreshToken", "invalidToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }
}