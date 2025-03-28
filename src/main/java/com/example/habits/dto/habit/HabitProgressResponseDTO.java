package com.example.habits.dto.habit;

import com.example.habits.domain.HabitProgress;
import lombok.Data;

import java.time.LocalDate;

@Data
public class HabitProgressResponseDTO {
    private Long id;
    private LocalDate date;
    private int completedAmount;
    private Long habitId;

    public static HabitProgressResponseDTO fromEntity(HabitProgress progress) {
        HabitProgressResponseDTO dto = new HabitProgressResponseDTO();
        dto.setId(progress.getId());
        dto.setDate(progress.getDate());
        dto.setCompletedAmount(progress.getCompletedAmount());
        dto.setHabitId(progress.getHabit().getId());
        return dto;
    }
}
