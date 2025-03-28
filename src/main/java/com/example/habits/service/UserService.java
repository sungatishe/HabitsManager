package com.example.habits.service;

import com.example.habits.domain.User;
import com.example.habits.dto.user.ProfileResponseDTO;
import com.example.habits.dto.user.UpdateProfileRequestDTO;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {
    Optional<ProfileResponseDTO> getUserById(Long id);
    ProfileResponseDTO getProfile(String username);
    ProfileResponseDTO updateUser(Long id, UpdateProfileRequestDTO request);
    List<ProfileResponseDTO> getAllUsers(int limit, int offset);
    void deleteUser(Long id);
    UserDetails loadUserByUsername(String username);
    User getCurrentUser(String username);
}
