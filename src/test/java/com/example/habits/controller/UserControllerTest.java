package com.example.habits.controller;

import com.example.habits.domain.User;
import com.example.habits.dto.user.ProfileResponseDTO;
import com.example.habits.dto.user.UpdateProfileRequestDTO;
import com.example.habits.exception.GlobalExceptionHandler;
import com.example.habits.exception.ResourceNotFoundException;
import com.example.habits.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    private User user;
    private ProfileResponseDTO profileResponseDTO;
    private UpdateProfileRequestDTO updateProfileRequestDTO;

    @BeforeEach
    void setUp() {
        // Настройка пользователя
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFullName("Test User");

        // Настройка DTO ответа
        profileResponseDTO = new ProfileResponseDTO();
        profileResponseDTO.setId(1L);
        profileResponseDTO.setUsername("testuser");
        profileResponseDTO.setEmail("test@example.com");
        profileResponseDTO.setFullName("Test User");

        // Настройка запроса на обновление
        updateProfileRequestDTO = new UpdateProfileRequestDTO();
        updateProfileRequestDTO.setEmail("newtest@example.com");
        updateProfileRequestDTO.setFullName("New Test User");

        // Настройка SecurityContext
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
        SecurityContextHolder.setContext(securityContext);

        // Настройка MockMvc с глобальным обработчиком исключений
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // Тесты для getOwnProfile
    @Test
    void shouldGetOwnProfileSuccessfully() throws Exception {
        when(userService.getProfile("testuser")).thenReturn(profileResponseDTO);

        mockMvc.perform(get("/api/v1/users/profile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"));
    }

    // Тесты для getProfile
    @Test
    void shouldGetProfileByIdSuccessfully() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.of(profileResponseDTO));

        mockMvc.perform(get("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"));
    }

    @Test
    void shouldReturnNotFoundWhenUserNotFoundById() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found with id: 1"));
    }

    // Тесты для updateProfile
    @Test
    void shouldUpdateProfileSuccessfully() throws Exception {
        ProfileResponseDTO updatedProfile = new ProfileResponseDTO();
        updatedProfile.setId(1L);
        updatedProfile.setUsername("testuser");
        updatedProfile.setEmail("newtest@example.com");
        updatedProfile.setFullName("New Test User");

        when(userService.getCurrentUser("testuser")).thenReturn(user);
        when(userService.updateUser(1L, updateProfileRequestDTO)).thenReturn(updatedProfile);

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"newtest@example.com\",\"fullName\":\"New Test User\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("newtest@example.com"))
                .andExpect(jsonPath("$.fullName").value("New Test User"));
    }

    @Test
    void shouldReturnNotFoundWhenUserNotFoundDuringUpdate() throws Exception {
        when(userService.getCurrentUser("testuser")).thenReturn(user);
        when(userService.updateUser(1L, updateProfileRequestDTO)).thenThrow(new ResourceNotFoundException("User not found with id: 1"));

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"newtest@example.com\",\"fullName\":\"New Test User\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found with id: 1"));
    }

    // Тесты для deleteUser
    @Test
    void shouldDeleteUserSuccessfully() throws Exception {
        when(userService.getCurrentUser("testuser")).thenReturn(user);

        mockMvc.perform(delete("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenUserNotFoundDuringDelete() throws Exception {
        when(userService.getCurrentUser("testuser")).thenReturn(user);
        doThrow(new ResourceNotFoundException("User not found with id: 1")).when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found with id: 1"));
    }

    // Тесты для getAllUsers
    @Test
    void shouldGetAllUsersSuccessfully() throws Exception {
        List<ProfileResponseDTO> users = Collections.singletonList(profileResponseDTO);
        when(userService.getAllUsers(10, 0)).thenReturn(users);

        mockMvc.perform(get("/api/v1/users")
                        .param("limit", "10")
                        .param("offset", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[0].email").value("test@example.com"))
                .andExpect(jsonPath("$[0].fullName").value("Test User"));
    }

    @Test
    void shouldReturnBadRequestWhenInvalidPagination() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .param("limit", "0")
                        .param("offset", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Limit must be greater than 0 and offset must be non-negative"));
    }
}