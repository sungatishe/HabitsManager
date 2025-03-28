package com.example.habits.service.impl;

import com.example.habits.domain.Habit;
import com.example.habits.domain.HabitProgress;
import com.example.habits.domain.User;
import com.example.habits.dto.habit.*;
import com.example.habits.repository.HabitProgressRepository;
import com.example.habits.repository.HabitRepository;
import com.example.habits.repository.UserRepository;
import com.example.habits.service.HabitService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class HabitServiceImpl implements HabitService {
    private final HabitRepository habitRepository;
    private final HabitProgressRepository habitProgressRepository;
    private final UserRepository userRepository;

    public HabitResponseDTO createHabit(HabitRequestDTO habitRequest) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        Habit habit = new Habit();
        habit.setName(habitRequest.getName());
        habit.setFrequency(habitRequest.getFrequency());
        habit.setTargetAmount(habitRequest.getTargetAmount());
        habit.setUser(user);
        Habit savedHabit = habitRepository.save(habit);

        return HabitResponseDTO.fromEntity(savedHabit);
    }

    public HabitProgressResponseDTO recordProgress(Long habitId, HabitProgressRequestDTO progressRequest) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found"));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!habit.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized access to habit");
        }

        HabitProgress progress = new HabitProgress();
        progress.setDate(LocalDate.now());
        progress.setCompletedAmount(progressRequest.getCompletedAmount());
        progress.setHabit(habit);
        HabitProgress savedHabitProgress = habitProgressRepository.save(progress);

        return HabitProgressResponseDTO.fromEntity(savedHabitProgress);
    }

    public HabitAnalyticsResponseDTO getAnalytics(Long habitId, String period) {
        Habit habit = habitRepository.findById(habitId)
                .orElseThrow(() -> new RuntimeException("Habit not found"));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("Requested by: " + username + ", Habit owner: " + habit.getUser().getUsername());
        if (!habit.getUser().getUsername().equals(username)) {
            System.out.println("Unauthorized access detected");
            throw new RuntimeException("Unauthorized access to habit");
        }

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = period.equals("week") ? endDate.minusDays(7) : endDate.minusMonths(1);

        List<HabitProgress> progressList = habitProgressRepository.findByHabitIdAndDateBetween(habitId, startDate, endDate);
        int totalTarget = progressList.size() * habit.getTargetAmount();
        int totalCompleted = progressList.stream().mapToInt(HabitProgress::getCompletedAmount).sum();
        double completionPercentage = totalTarget > 0 ? (double) totalCompleted / totalTarget * 100 : 0;

        HabitAnalyticsResponseDTO analytics = new HabitAnalyticsResponseDTO();
        analytics.setHabitName(habit.getName());
        analytics.setPeriod(period);
        analytics.setTotalTarget(totalTarget);
        analytics.setTotalCompleted(totalCompleted);
        analytics.setCompletionPercentage(String.format(Locale.US, "%.2f%%", completionPercentage)); // Используем Locale.US
        return analytics;
    }

}
