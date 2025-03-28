package com.example.habits.controller;

import com.example.habits.dto.habit.*;
import com.example.habits.service.HabitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/habits")
@RequiredArgsConstructor
public class HabitController {

    private final HabitService habitService;

    @PostMapping
    public ResponseEntity<HabitResponseDTO> createHabit(@Valid @RequestBody HabitRequestDTO habitRequest) {
        HabitResponseDTO response = habitService.createHabit(habitRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{habitId}/progress")
    public ResponseEntity<HabitProgressResponseDTO> recordProgress(
            @PathVariable Long habitId,
            @Valid @RequestBody HabitProgressRequestDTO request) {
        HabitProgressResponseDTO response = habitService.recordProgress(habitId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{habitId}/analytics")
    public ResponseEntity<HabitAnalyticsResponseDTO> getAnalytics(
            @PathVariable Long habitId,
            @RequestParam String period) {
        HabitAnalyticsResponseDTO response = habitService.getAnalytics(habitId, period);
        return ResponseEntity.ok(response);
    }

}
