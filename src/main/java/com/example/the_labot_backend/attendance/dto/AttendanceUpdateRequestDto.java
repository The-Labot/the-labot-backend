package com.example.the_labot_backend.attendance.dto;

import com.example.the_labot_backend.attendance.AttendanceStatus;
import lombok.Data;

import java.time.LocalTime;

@Data
public class AttendanceUpdateRequestDto {
    private LocalTime clockInTime;  // 수정할 출근 시간 (예: "08:15")
    private LocalTime clockOutTime; // 수정할 퇴근 시간 (예: "18:00")
    private AttendanceStatus status; // 수정할 상태 (예: "정상")
}
