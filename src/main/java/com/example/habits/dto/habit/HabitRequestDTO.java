package com.example.habits.dto.habit;

import com.example.habits.domain.Frequency;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HabitRequestDTO {

    @NotBlank(message = "Name cannot be empty")
    private String name;

    @NotNull(message = "Frequency cannot be null")
    private Frequency frequency;

    @Min(value = 1, message = "Target amount must be at least 1")
    private int targetAmount;
}
