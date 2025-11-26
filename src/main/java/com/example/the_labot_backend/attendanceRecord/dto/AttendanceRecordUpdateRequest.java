package com.example.the_labot_backend.attendanceRecord.dto;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class AttendanceRecordUpdateRequest {
    private LocalDate date;
    private Double actualBasicWork;       // 실제 기본근로
    private Double actualExtendedWork;    // 실제 연장근로
    private Double actualOvertimeWork;    // 실제 초과근로
    private Double actualNightWork;       // 실제 야간근로
    private Double actualHolidayWork;     // 실제 휴일근로
    private Long unitPrice;   // 기준단가
}
