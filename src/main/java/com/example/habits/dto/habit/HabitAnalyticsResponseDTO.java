package com.example.habits.dto.habit;

import lombok.Data;

@Data
public class HabitAnalyticsResponseDTO {

    private String habitName;
    private String period;
    private int totalTarget;
    private int totalCompleted;
    private String completionPercentage;

}
