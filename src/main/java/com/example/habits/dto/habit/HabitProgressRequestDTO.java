package com.example.habits.dto.habit;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class HabitProgressRequestDTO {
    @Min(value = 0, message = "Completed amount cannot be negative")
    private int completedAmount;
}
