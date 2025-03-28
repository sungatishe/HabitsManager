package com.example.habits.repository;

import com.example.habits.domain.HabitProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface HabitProgressRepository extends JpaRepository<HabitProgress, Long> {
    List<HabitProgress> findByHabitIdAndDateBetween(Long habitId, LocalDate startDate, LocalDate endDate);
}
