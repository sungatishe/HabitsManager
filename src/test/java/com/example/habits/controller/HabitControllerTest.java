package com.example.habits.controller;

import com.example.habits.domain.Frequency;
import com.example.habits.dto.habit.*;
import com.example.habits.exception.GlobalExceptionHandler;
import com.example.habits.exception.ResourceNotFoundException;
import com.example.habits.service.HabitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HabitControllerTest {

    private MockMvc mockMvc;

    @Mock
    private HabitService habitService;

    @InjectMocks
    private HabitController habitController;

    private HabitRequestDTO habitRequestDTO;
    private HabitResponseDTO habitResponseDTO;
    private HabitProgressRequestDTO habitProgressRequestDTO;
    private HabitProgressResponseDTO habitProgressResponseDTO;
    private HabitAnalyticsResponseDTO habitAnalyticsResponseDTO;

    @BeforeEach
    void setUp() {
        // Настройка DTO для создания привычки
        habitRequestDTO = new HabitRequestDTO();
        habitRequestDTO.setName("Morning Run");
        habitRequestDTO.setFrequency(Frequency.DAILY);
        habitRequestDTO.setTargetAmount(1);

        // Настройка DTO ответа для привычки
        habitResponseDTO = new HabitResponseDTO();
        habitResponseDTO.setId(1L);
        habitResponseDTO.setName("Morning Run");
        habitResponseDTO.setFrequency(Frequency.DAILY);
        habitResponseDTO.setTargetAmount(1);
        habitResponseDTO.setUserId(1L);

        // Настройка DTO для записи прогресса
        habitProgressRequestDTO = new HabitProgressRequestDTO();
        habitProgressRequestDTO.setCompletedAmount(1);

        // Настройка DTO ответа для прогресса
        habitProgressResponseDTO = new HabitProgressResponseDTO();
        habitProgressResponseDTO.setId(1L);
        habitProgressResponseDTO.setDate(LocalDate.now());
        habitProgressResponseDTO.setCompletedAmount(1);
        habitProgressResponseDTO.setHabitId(1L);

        // Настройка DTO для аналитики
        habitAnalyticsResponseDTO = new HabitAnalyticsResponseDTO();
        habitAnalyticsResponseDTO.setHabitName("Morning Run");
        habitAnalyticsResponseDTO.setPeriod("WEEKLY");
        habitAnalyticsResponseDTO.setTotalTarget(7);
        habitAnalyticsResponseDTO.setTotalCompleted(5);
        habitAnalyticsResponseDTO.setCompletionPercentage("71.43%");

        // Настройка MockMvc с глобальным обработчиком исключений
        mockMvc = MockMvcBuilders.standaloneSetup(habitController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // Тесты для createHabit
    @Test
    void shouldCreateHabitSuccessfully() throws Exception {
        when(habitService.createHabit(any(HabitRequestDTO.class))).thenReturn(habitResponseDTO);

        mockMvc.perform(post("/api/v1/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Morning Run\",\"frequency\":\"DAILY\",\"targetAmount\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Morning Run"))
                .andExpect(jsonPath("$.frequency").value("DAILY"))
                .andExpect(jsonPath("$.targetAmount").value(1))
                .andExpect(jsonPath("$.userId").value(1L));
    }

    @Test
    void shouldReturnBadRequestWhenCreateHabitWithInvalidData() throws Exception {
        mockMvc.perform(post("/api/v1/habits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"frequency\":null,\"targetAmount\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name cannot be empty"))
                .andExpect(jsonPath("$.frequency").value("Frequency cannot be null"))
                .andExpect(jsonPath("$.targetAmount").value("Target amount must be at least 1"));
    }

    // Тесты для recordProgress
    @Test
    void shouldRecordProgressSuccessfully() throws Exception {
        when(habitService.recordProgress(eq(1L), any(HabitProgressRequestDTO.class))).thenReturn(habitProgressResponseDTO);

        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();

        mockMvc.perform(post("/api/v1/habits/1/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"completedAmount\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.date[0]").value(year)) // Год
                .andExpect(jsonPath("$.date[1]").value(month)) // Месяц
                .andExpect(jsonPath("$.date[2]").value(day)) // День
                .andExpect(jsonPath("$.completedAmount").value(1))
                .andExpect(jsonPath("$.habitId").value(1L));
    }

    @Test
    void shouldReturnBadRequestWhenRecordProgressWithInvalidData() throws Exception {
        mockMvc.perform(post("/api/v1/habits/1/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"completedAmount\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.completedAmount").value("Completed amount cannot be negative"));
    }

    @Test
    void shouldReturnNotFoundWhenRecordProgressForNonExistentHabit() throws Exception {
        when(habitService.recordProgress(eq(1L), any(HabitProgressRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Habit not found with id: 1"));

        mockMvc.perform(post("/api/v1/habits/1/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"completedAmount\":1}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Habit not found with id: 1"));
    }

    // Тесты для getAnalytics
    @Test
    void shouldGetAnalyticsSuccessfully() throws Exception {
        when(habitService.getAnalytics(eq(1L), eq("WEEKLY"))).thenReturn(habitAnalyticsResponseDTO);

        mockMvc.perform(get("/api/v1/habits/1/analytics")
                        .param("period", "WEEKLY")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.habitName").value("Morning Run"))
                .andExpect(jsonPath("$.period").value("WEEKLY"))
                .andExpect(jsonPath("$.totalTarget").value(7))
                .andExpect(jsonPath("$.totalCompleted").value(5))
                .andExpect(jsonPath("$.completionPercentage").value("71.43%"));
    }

    @Test
    void shouldReturnNotFoundWhenGetAnalyticsForNonExistentHabit() throws Exception {
        when(habitService.getAnalytics(eq(1L), eq("WEEKLY")))
                .thenThrow(new ResourceNotFoundException("Habit not found with id: 1"));

        mockMvc.perform(get("/api/v1/habits/1/analytics")
                        .param("period", "WEEKLY")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Habit not found with id: 1"));
    }

    @Test
    void shouldReturnBadRequestWhenGetAnalyticsWithInvalidPeriod() throws Exception {
        when(habitService.getAnalytics(eq(1L), eq("INVALID")))
                .thenThrow(new IllegalArgumentException("Invalid period: INVALID"));

        mockMvc.perform(get("/api/v1/habits/1/analytics")
                        .param("period", "INVALID")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid period: INVALID"));
    }
}