package com.example.habits.service;

import com.example.habits.domain.User;
import com.example.habits.dto.user.ProfileResponseDTO;
import com.example.habits.dto.user.UpdateProfileRequestDTO;
import com.example.habits.repository.UserRepository;
import com.example.habits.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UpdateProfileRequestDTO updateProfileRequest;

    @BeforeEach
    void setUp() {
        // Настройка пользователя
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFullName("Test User");

        // Настройка запроса на обновление профиля
        updateProfileRequest = new UpdateProfileRequestDTO();
        updateProfileRequest.setEmail("newtest@example.com");
        updateProfileRequest.setFullName("New Test User");
    }

    // Тесты для getUserById
    @Test
    void shouldGetUserByIdSuccessfully() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        Optional<ProfileResponseDTO> response = userService.getUserById(1L);

        // Then
        assertThat(response).isPresent();
        assertThat(response.get().getId()).isEqualTo(1L);
        assertThat(response.get().getUsername()).isEqualTo("testuser");
        assertThat(response.get().getEmail()).isEqualTo("test@example.com");
        assertThat(response.get().getFullName()).isEqualTo("Test User");

        verify(userRepository).findById(1L);
    }

    @Test
    void shouldReturnEmptyWhenUserNotFoundById() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        Optional<ProfileResponseDTO> response = userService.getUserById(1L);

        // Then
        assertThat(response).isNotPresent();

        verify(userRepository).findById(1L);
    }

    // Тесты для getProfile
    @Test
    void shouldGetProfileSuccessfully() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When
        ProfileResponseDTO response = userService.getProfile("testuser");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getFullName()).isEqualTo("Test User");

        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundByUsername() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getProfile("testuser"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found with username: testuser");

        verify(userRepository).findByUsername("testuser");
    }

    // Тесты для updateUser
    @Test
    void shouldUpdateUserSuccessfully() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        ProfileResponseDTO response = userService.updateUser(1L, updateProfileRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("newtest@example.com");
        assertThat(response.getFullName()).isEqualTo("New Test User");
        assertThat(response.getUsername()).isEqualTo("testuser");

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundDuringUpdate() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(1L, updateProfileRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found with id: 1");

        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    // Тесты для deleteUser
    @Test
    void shouldDeleteUserSuccessfully() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundDuringDelete() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found with id: 1");

        verify(userRepository).existsById(1L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    // Тесты для getAllUsers
    @Test
    void shouldGetAllUsersSuccessfully() {
        // Given
        List<User> users = Collections.singletonList(user);
        Page<User> userPage = new PageImpl<>(users);
        when(userRepository.findAll(PageRequest.of(0, 10))).thenReturn(userPage);

        // When
        List<ProfileResponseDTO> response = userService.getAllUsers(10, 0);

        // Then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getId()).isEqualTo(1L);
        assertThat(response.get(0).getUsername()).isEqualTo("testuser");
        assertThat(response.get(0).getEmail()).isEqualTo("test@example.com");
        assertThat(response.get(0).getFullName()).isEqualTo("Test User");

        verify(userRepository).findAll(PageRequest.of(0, 10));
    }

    // Тесты для loadUserByUsername
    @Test
    void shouldLoadUserByUsernameSuccessfully() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When
        UserDetails userDetails = userService.loadUserByUsername("testuser");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo("password");
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");

        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundByUsernameDuringLoad() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.loadUserByUsername("testuser"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found with username: testuser");

        verify(userRepository).findByUsername("testuser");
    }

    // Тесты для getCurrentUser
    @Test
    void shouldGetCurrentUserSuccessfully() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When
        User currentUser = userService.getCurrentUser("testuser");

        // Then
        assertThat(currentUser).isNotNull();
        assertThat(currentUser.getId()).isEqualTo(1L);
        assertThat(currentUser.getUsername()).isEqualTo("testuser");
        assertThat(currentUser.getEmail()).isEqualTo("test@example.com");
        assertThat(currentUser.getFullName()).isEqualTo("Test User");

        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundDuringGetCurrentUser() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getCurrentUser("testuser"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found with username: testuser");

        verify(userRepository).findByUsername("testuser");
    }
}