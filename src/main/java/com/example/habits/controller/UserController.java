package com.example.habits.controller;

import com.example.habits.domain.User;
import com.example.habits.dto.user.UpdateProfileRequestDTO;
import com.example.habits.exception.ResourceNotFoundException;
import com.example.habits.dto.user.ProfileResponseDTO;
import com.example.habits.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API for managing user profiles")
@SecurityRequirement(name = "BearerAuth")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;


    @Operation(summary = "Get the profile of the authenticated user", description = "Retrieves the profile of the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Access denied")
    })
    @GetMapping("/profile")
    public ResponseEntity<ProfileResponseDTO> getOwnProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("User {} requested their own profile", username);
        ProfileResponseDTO profile = userService.getProfile(username);
        return ResponseEntity.ok(profile);
    }

    @Operation(summary = "Get a user's profile by ID", description = "Retrieves the profile of a user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponseDTO> getProfile(@PathVariable Long userId) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("User {} requested profile of user with ID {}", currentUsername, userId);
        ProfileResponseDTO profile = userService.getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return ResponseEntity.ok(profile);
    }


    @Operation(summary = "Update a user's profile by ID", description = "Updates the profile of a user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/{userId}")
    public ResponseEntity<ProfileResponseDTO> updateProfile(
            @Valid @RequestBody UpdateProfileRequestDTO request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getCurrentUser(currentUsername);
        ProfileResponseDTO updatedProfile = userService.updateUser(currentUser.getId(), request);
        return ResponseEntity.ok(updatedProfile);
    }



    @Operation(summary = "Delete a user by ID", description = "Deletes a user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getCurrentUser(currentUsername);
        userService.deleteUser(currentUser.getId());
        return ResponseEntity.noContent().build();
    }



    @Operation(summary = "Get a paginated list of users", description = "Retrieves a list of users with pagination using limit and offset")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Access denied")
    })
    @GetMapping
    public ResponseEntity<List<ProfileResponseDTO>> getAllUsers(
            @Parameter(description = "Maximum number of users to return") @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Number of users to skip (offset)") @RequestParam(defaultValue = "0") int offset) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("User {} requested list of users with limit={} and offset={}", currentUsername, limit, offset);
        if (limit <= 0 || offset < 0) {
            throw new IllegalArgumentException("Limit must be greater than 0 and offset must be non-negative");
        }
        List<ProfileResponseDTO> users = userService.getAllUsers(limit, offset);
        return ResponseEntity.ok(users);
    }

}
