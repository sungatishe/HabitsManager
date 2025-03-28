package com.example.habits.dto.user;

import com.example.habits.domain.User;
import lombok.Data;

@Data
public class ProfileResponseDTO {
    private Long id;
    private String username;
    private String email;
    private String fullName;

    public static ProfileResponseDTO fromEntity(User user) {
        ProfileResponseDTO dto = new ProfileResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        return dto;
    }
}
