package com.example.habits.service;

import com.example.habits.domain.RefreshToken;
import com.example.habits.domain.User;
import com.example.habits.dto.auth.LoginRequestDTO;
import com.example.habits.dto.auth.RegisterRequestDTO;
import com.example.habits.dto.auth.TokenResponseDTO;
import com.example.habits.repository.RefreshTokenRepository;
import com.example.habits.repository.UserRepository;
import com.example.habits.service.impl.AuthenticationServiceImpl;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private User user;
    private RegisterRequestDTO registerRequest;
    private LoginRequestDTO loginRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setFullName("Test User");

        registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password");
        registerRequest.setFullName("Test User");

        loginRequest = new LoginRequestDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User registeredUser = authenticationService.registerUser(registerRequest);

        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.getUsername()).isEqualTo("testuser");
        assertThat(registeredUser.getEmail()).isEqualTo("test@example.com");
        assertThat(registeredUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(registeredUser.getFullName()).isEqualTo("Test User");

        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExistsDuringRegistration() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authenticationService.registerUser(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username already exist");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExistsDuringRegistration() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authenticationService.registerUser(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldLoginUserSuccessfully() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(user)).thenReturn("refreshToken");
        when(jwtService.extractClaims("refreshToken")).thenReturn(mock(Claims.class));
        when(jwtService.extractClaims("refreshToken").getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 3600000));

        // When
        TokenResponseDTO response = authenticationService.loginUser(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken");

        verify(refreshTokenRepository).deleteByUserId(user.getId());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void shouldThrowExceptionWhenLoginFailsWithInvalidCredentials() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new IllegalArgumentException("Invalid credentials"));

        // When & Then
        assertThatThrownBy(() -> authenticationService.loginUser(loginRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid credentials");

        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundDuringLogin() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authenticationService.loginUser(loginRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");

        verify(jwtService, never()).generateAccessToken(any(User.class));
    }

    // Тесты для refreshAccessToken
    @Test
    void shouldRefreshAccessTokenSuccessfully() {
        // Given
        String refreshToken = "refreshToken";
        RefreshToken storedToken = new RefreshToken();
        storedToken.setToken(refreshToken);
        storedToken.setUser(user);
        storedToken.setExpiryTime(Instant.now().plusSeconds(3600));

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("testuser");
        when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 3600000));

        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(storedToken));
        when(jwtService.isTokenValid(refreshToken)).thenReturn(true);
        when(jwtService.isTokenExpired(refreshToken)).thenReturn(false);
        when(jwtService.extractClaims(refreshToken)).thenReturn(claims);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("newAccessToken");
        when(jwtService.generateRefreshToken(user)).thenReturn("newRefreshToken");
        when(jwtService.extractClaims("newRefreshToken")).thenReturn(claims);

        // When
        TokenResponseDTO response = authenticationService.refreshAccessToken(refreshToken);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
        assertThat(response.getRefreshToken()).isEqualTo("newRefreshToken");

        verify(refreshTokenRepository).deleteByToken(refreshToken);
        verify(refreshTokenRepository).deleteByUserId(user.getId());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void shouldThrowExceptionWhenRefreshTokenNotFound() {
        // Given
        String refreshToken = "refreshToken";
        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authenticationService.refreshAccessToken(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh token not found");

        verify(jwtService, never()).isTokenValid(anyString());
    }

    @Test
    void shouldThrowExceptionWhenRefreshTokenIsInvalid() {
        // Given
        String refreshToken = "refreshToken";
        RefreshToken storedToken = new RefreshToken();
        storedToken.setToken(refreshToken);
        storedToken.setUser(user);
        storedToken.setExpiryTime(Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(storedToken));
        when(jwtService.isTokenValid(refreshToken)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authenticationService.refreshAccessToken(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid refresh token");

        verify(refreshTokenRepository).deleteByToken(refreshToken);
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void shouldThrowExceptionWhenRefreshTokenIsExpired() {
        // Given
        String refreshToken = "refreshToken";
        RefreshToken storedToken = new RefreshToken();
        storedToken.setToken(refreshToken);
        storedToken.setUser(user);
        storedToken.setExpiryTime(Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(storedToken));
        when(jwtService.isTokenValid(refreshToken)).thenReturn(true);
        when(jwtService.isTokenExpired(refreshToken)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authenticationService.refreshAccessToken(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh token expired");

        verify(refreshTokenRepository).deleteByToken(refreshToken);
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void shouldThrowExceptionWhenRefreshTokenDoesNotBelongToUser() {
        // Given
        String refreshToken = "refreshToken";
        User differentUser = new User();
        differentUser.setId(2L);
        differentUser.setUsername("differentuser");

        RefreshToken storedToken = new RefreshToken();
        storedToken.setToken(refreshToken);
        storedToken.setUser(differentUser);
        storedToken.setExpiryTime(Instant.now().plusSeconds(3600));

        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("testuser");

        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(storedToken));
        when(jwtService.isTokenValid(refreshToken)).thenReturn(true);
        when(jwtService.isTokenExpired(refreshToken)).thenReturn(false);
        when(jwtService.extractClaims(refreshToken)).thenReturn(claims);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> authenticationService.refreshAccessToken(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh token does not belong to user");

        verify(refreshTokenRepository).deleteByToken(refreshToken);
        verify(jwtService, never()).generateAccessToken(any(User.class));
    }
}
