package com.example.the_labot_backend.attendanceRecord.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class AttendanceDailyResponse {
    private LocalDate date;
    private Double manHour;
}
