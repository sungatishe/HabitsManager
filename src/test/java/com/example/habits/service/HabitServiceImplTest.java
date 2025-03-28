package com.example.habits.service;

import com.example.habits.domain.Frequency;
import com.example.habits.domain.Habit;
import com.example.habits.domain.HabitProgress;
import com.example.habits.domain.User;
import com.example.habits.dto.habit.HabitAnalyticsResponseDTO;
import com.example.habits.dto.habit.HabitProgressRequestDTO;
import com.example.habits.dto.habit.HabitProgressResponseDTO;
import com.example.habits.dto.habit.HabitRequestDTO;
import com.example.habits.dto.habit.HabitResponseDTO;
import com.example.habits.repository.HabitProgressRepository;
import com.example.habits.repository.HabitRepository;
import com.example.habits.repository.UserRepository;
import com.example.habits.service.impl.HabitServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HabitServiceImplTest {

    @Mock
    private HabitRepository habitRepository;

    @Mock
    private HabitProgressRepository habitProgressRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private HabitServiceImpl habitService;

    private User user;
    private Habit habit;
    private HabitRequestDTO habitRequest;

    @BeforeEach
    void setUp() {
        // Настройка пользователя
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFullName("Test User");

        // Настройка привычки
        habit = new Habit();
        habit.setId(1L);
        habit.setName("Morning Run");
        habit.setFrequency(Frequency.DAILY);
        habit.setTargetAmount(1);
        habit.setUser(user);

        // Настройка запроса на создание привычки
        habitRequest = new HabitRequestDTO();
        habitRequest.setName("Morning Run");
        habitRequest.setFrequency(Frequency.DAILY);
        habitRequest.setTargetAmount(1);
    }

    private void setupSecurityContext() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
    }

    // Тесты для createHabit
    @Test
    void shouldCreateHabitSuccessfully() {
        // Given
        setupSecurityContext();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(habitRepository.save(any(Habit.class))).thenReturn(habit);

        // When
        HabitResponseDTO response = habitService.createHabit(habitRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Morning Run");
        assertThat(response.getFrequency()).isEqualTo(Frequency.DAILY);
        assertThat(response.getTargetAmount()).isEqualTo(1);

        verify(habitRepository).save(any(Habit.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundDuringCreateHabit() {
        // Given
        setupSecurityContext();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> habitService.createHabit(habitRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");

        verify(habitRepository, never()).save(any(Habit.class));
    }

    // Тесты для recordProgress
    @Test
    void shouldRecordProgressSuccessfully() {
        // Given
        setupSecurityContext();
        HabitProgressRequestDTO progressRequest = new HabitProgressRequestDTO();
        progressRequest.setCompletedAmount(1);

        HabitProgress progress = new HabitProgress();
        progress.setId(1L);
        progress.setHabit(habit);
        progress.setDate(LocalDate.now());
        progress.setCompletedAmount(1);

        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        when(habitProgressRepository.save(any(HabitProgress.class))).thenReturn(progress);

        // When
        HabitProgressResponseDTO response = habitService.recordProgress(1L, progressRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getCompletedAmount()).isEqualTo(1);
        assertThat(response.getDate()).isEqualTo(LocalDate.now());

        verify(habitProgressRepository).save(any(HabitProgress.class));
    }

    @Test
    void shouldThrowExceptionWhenHabitNotFoundDuringRecordProgress() {
        // Given
        HabitProgressRequestDTO progressRequest = new HabitProgressRequestDTO();
        progressRequest.setCompletedAmount(1);

        when(habitRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> habitService.recordProgress(1L, progressRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Habit not found");

        verify(habitProgressRepository, never()).save(any(HabitProgress.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotAuthorizedForHabitDuringRecordProgress() {
        // Given
        setupSecurityContext();
        HabitProgressRequestDTO progressRequest = new HabitProgressRequestDTO();
        progressRequest.setCompletedAmount(1);

        User differentUser = new User();
        differentUser.setId(2L);
        differentUser.setUsername("differentuser");

        Habit habitWithDifferentUser = new Habit();
        habitWithDifferentUser.setId(1L);
        habitWithDifferentUser.setUser(differentUser);

        when(habitRepository.findById(1L)).thenReturn(Optional.of(habitWithDifferentUser));

        // When & Then
        assertThatThrownBy(() -> habitService.recordProgress(1L, progressRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Unauthorized access to habit");

        verify(habitProgressRepository, never()).save(any(HabitProgress.class));
    }

    // Тесты для getAnalytics
    @Test
    void shouldGetAnalyticsForWeekSuccessfully() {
        // Given
        setupSecurityContext();
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(7);

        HabitProgress progress1 = new HabitProgress();
        progress1.setHabit(habit);
        progress1.setDate(today);
        progress1.setCompletedAmount(1);

        HabitProgress progress2 = new HabitProgress();
        progress2.setHabit(habit);
        progress2.setDate(today.minusDays(1));
        progress2.setCompletedAmount(1);

        List<HabitProgress> progressList = List.of(progress1, progress2);

        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        when(habitProgressRepository.findByHabitIdAndDateBetween(eq(1L), eq(startDate), eq(today)))
                .thenReturn(progressList);

        // When
        HabitAnalyticsResponseDTO response = habitService.getAnalytics(1L, "week");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getHabitName()).isEqualTo("Morning Run");
        assertThat(response.getPeriod()).isEqualTo("week");
        assertThat(response.getTotalTarget()).isEqualTo(2); // 2 days * 1 target
        assertThat(response.getTotalCompleted()).isEqualTo(2); // 1 + 1
        assertThat(response.getCompletionPercentage()).isEqualTo("100.00%");
    }

    @Test
    void shouldGetAnalyticsForMonthSuccessfully() {
        // Given
        setupSecurityContext();
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusMonths(1);

        HabitProgress progress1 = new HabitProgress();
        progress1.setHabit(habit);
        progress1.setDate(today);
        progress1.setCompletedAmount(1);

        List<HabitProgress> progressList = List.of(progress1);

        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        when(habitProgressRepository.findByHabitIdAndDateBetween(eq(1L), eq(startDate), eq(today)))
                .thenReturn(progressList);

        // When
        HabitAnalyticsResponseDTO response = habitService.getAnalytics(1L, "month");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getHabitName()).isEqualTo("Morning Run");
        assertThat(response.getPeriod()).isEqualTo("month");
        assertThat(response.getTotalTarget()).isEqualTo(1); // 1 day * 1 target
        assertThat(response.getTotalCompleted()).isEqualTo(1); // 1
        assertThat(response.getCompletionPercentage()).isEqualTo("100.00%");
    }

    @Test
    void shouldThrowExceptionWhenHabitNotFoundDuringGetAnalytics() {
        // Given
        when(habitRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> habitService.getAnalytics(1L, "week"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Habit not found");

        verify(habitProgressRepository, never()).findByHabitIdAndDateBetween(anyLong(), any(), any());
    }

    @Test
    void shouldThrowExceptionWhenUserNotAuthorizedForHabitDuringGetAnalytics() {
        // Given
        setupSecurityContext();
        User differentUser = new User();
        differentUser.setId(2L);
        differentUser.setUsername("differentuser");

        Habit habitWithDifferentUser = new Habit();
        habitWithDifferentUser.setId(1L);
        habitWithDifferentUser.setUser(differentUser);

        when(habitRepository.findById(1L)).thenReturn(Optional.of(habitWithDifferentUser));

        // When & Then
        assertThatThrownBy(() -> habitService.getAnalytics(1L, "week"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Unauthorized access to habit");

        verify(habitProgressRepository, never()).findByHabitIdAndDateBetween(anyLong(), any(), any());
    }

    @Test
    void shouldReturnZeroCompletionPercentageWhenNoProgress() {
        // Given
        setupSecurityContext();
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(7);

        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        when(habitProgressRepository.findByHabitIdAndDateBetween(eq(1L), eq(startDate), eq(today)))
                .thenReturn(List.of());

        // When
        HabitAnalyticsResponseDTO response = habitService.getAnalytics(1L, "week");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTotalTarget()).isEqualTo(0);
        assertThat(response.getTotalCompleted()).isEqualTo(0);
        assertThat(response.getCompletionPercentage()).isEqualTo("0.00%");
    }
}