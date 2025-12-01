package com.example.the_labot_backend.attendanceRecord.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class AttendanceMonthlyResponse {
    private int year;
    private int month;
    private List<AttendanceDailyResponse> records;
}
