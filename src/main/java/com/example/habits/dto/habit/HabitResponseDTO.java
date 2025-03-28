package com.example.habits.dto.habit;

import com.example.habits.domain.Frequency;
import com.example.habits.domain.Habit;
import lombok.Data;

@Data
public class HabitResponseDTO {
    private Long id;
    private String name;
    private Frequency frequency;
    private int targetAmount;
    private Long userId;

    public static HabitResponseDTO fromEntity(Habit habit) {
        HabitResponseDTO dto = new HabitResponseDTO();
        dto.setId(habit.getId());
        dto.setName(habit.getName());
        dto.setFrequency(habit.getFrequency());
        dto.setTargetAmount(habit.getTargetAmount());
        dto.setUserId(habit.getUser().getId());
        return dto;
    }
}
