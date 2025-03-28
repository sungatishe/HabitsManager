package com.example.habits.repository;

import com.example.habits.domain.Habit;
import com.example.habits.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static com.example.habits.domain.Frequency.DAILY;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class HabitRepositoryTest {

    @Autowired
    private HabitRepository habitRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Habit habit;

    @BeforeEach
    void setUp() {
        user = new User();
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
    }

    @Test
    void shouldFindHabitsByUserId() {
        List<Habit> habits = habitRepository.findByUserId(user.getId());
        assertThat(habits).hasSize(1);
        assertThat(habits.get(0).getName()).isEqualTo("Morning Run km");
        assertThat(habits.get(0).getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void shouldNotFindHabitsForNonExistentUser() {
        List<Habit> habits = habitRepository.findByUserId(999L);
        assertThat(habits).isEmpty();
    }

    @Test
    void shouldSaveHabit() {
        Habit newHabit = new Habit();
        newHabit.setUser(user);
        newHabit.setName("Evening Yoga");
        newHabit.setTargetAmount(30);
        newHabit.setFrequency(DAILY);

        Habit savedHabit = habitRepository.save(newHabit);

        assertThat(savedHabit.getId()).isNotNull();
        assertThat(savedHabit.getName()).isEqualTo("Evening Yoga");
    }

    @Test
    void shouldDeleteHabit() {
        habitRepository.deleteById(habit.getId());
        assertThat(habitRepository.findById(habit.getId())).isNotPresent();
    }

}
