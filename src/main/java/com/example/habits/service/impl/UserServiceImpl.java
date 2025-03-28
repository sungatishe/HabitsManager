package com.example.habits.service.impl;

import com.example.habits.domain.User;
import com.example.habits.dto.user.ProfileResponseDTO;
import com.example.habits.dto.user.UpdateProfileRequestDTO;
import com.example.habits.repository.UserRepository;
import com.example.habits.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;


    @Override
    public Optional<ProfileResponseDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(ProfileResponseDTO::fromEntity);
    }

    @Override
    public ProfileResponseDTO getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return ProfileResponseDTO.fromEntity(user);
    }


    @Override
    public ProfileResponseDTO updateUser(Long id, UpdateProfileRequestDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        User updatedUser = userRepository.save(user);
        return ProfileResponseDTO.fromEntity(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }


    @Override
    public List<ProfileResponseDTO> getAllUsers(int limit, int offset) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.getContent().stream()
                .map(ProfileResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }


    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Override
    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}
