package com.example.habits.service;

import com.example.habits.dto.habit.*;

public interface HabitService {
    HabitResponseDTO createHabit(HabitRequestDTO request);
    HabitProgressResponseDTO recordProgress(Long habitId, HabitProgressRequestDTO request);
    HabitAnalyticsResponseDTO getAnalytics(Long habitId, String period);
}
