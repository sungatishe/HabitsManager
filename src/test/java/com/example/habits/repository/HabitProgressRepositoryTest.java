package com.example.habits.repository;


import com.example.habits.domain.Habit;
import com.example.habits.domain.HabitProgress;
import com.example.habits.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;

import static com.example.habits.domain.Frequency.DAILY;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class HabitProgressRepositoryTest {

    @Autowired
    private HabitProgressRepository habitProgressRepository;

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private UserRepository userRepository;

    private Habit habit;
    private HabitProgress progress;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFullName("Test User");
        userRepository.save(user);

        habit = new Habit();
        habit.setUser(user);
        habit.setName("Morning Run km");
        habit.setFrequency(DAILY);
        habit.setTargetAmount(3);
        habitRepository.save(habit);

        progress = new HabitProgress();
        progress.setHabit(habit);
        progress.setDate(LocalDate.of(2025, 3, 14));
        progress.setCompletedAmount(1);
        habitProgressRepository.save(progress);
    }

    @Test
    void shouldFindHabitProgressByHabitIdAndDateBetween() {
        LocalDate startDate = LocalDate.of(2025, 3, 13);
        LocalDate endDate = LocalDate.of(2025, 3, 15);

        List<HabitProgress> progressList = habitProgressRepository.findByHabitIdAndDateBetween(habit.getId(), startDate, endDate);

        assertThat(progressList).hasSize(1);
        assertThat(progressList.get(0).getDate()).isEqualTo(LocalDate.of(2025, 3, 14));
        assertThat(progressList.get(0).getCompletedAmount()).isEqualTo(1);
    }

    @Test
    void shouldNotFindHabitProgressForNonExistentHabit() {
        LocalDate startDate = LocalDate.of(2025, 3, 13);
        LocalDate endDate = LocalDate.of(2025, 3, 15);

        List<HabitProgress> progressList = habitProgressRepository.findByHabitIdAndDateBetween(999L, startDate, endDate);

        assertThat(progressList).isEmpty();
    }

    @Test
    void shouldNotFindHabitProgressOutsideDateRange() {
        LocalDate startDate = LocalDate.of(2025, 3, 15);
        LocalDate endDate = LocalDate.of(2025, 3, 16);

        List<HabitProgress> progressList = habitProgressRepository.findByHabitIdAndDateBetween(habit.getId(), startDate, endDate);

        assertThat(progressList).isEmpty();
    }

    @Test
    void shouldSaveHabitProgress() {
        HabitProgress newProgress = new HabitProgress();
        newProgress.setHabit(habit);
        newProgress.setDate(LocalDate.of(2025, 3, 15));
        newProgress.setCompletedAmount(1);

        HabitProgress savedProgress = habitProgressRepository.save(newProgress);

        assertThat(savedProgress.getId()).isNotNull();
        assertThat(savedProgress.getDate()).isEqualTo(LocalDate.of(2025, 3, 15));
    }

    @Test
    void shouldDeleteHabitProgress() {
        habitProgressRepository.deleteById(progress.getId());
        assertThat(habitProgressRepository.findById(progress.getId())).isNotPresent();
    }
}
