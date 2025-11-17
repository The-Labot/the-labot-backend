package com.example.the_labot_backend.attendance.dto;

import com.example.the_labot_backend.attendance.Attendance;
import com.example.the_labot_backend.attendance.AttendanceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
@Data
@Builder
public class ClockInOutResponseDto {
    private Long attendanceId;  // 방금 생성/수정된 기록의 ID
    private LocalDate date;       // 날짜
    private LocalTime clockInTime;  // 출근 시간
    private LocalTime clockOutTime; // 퇴근 시간
    private AttendanceStatus status;   // 상태 (null일 수 있음)

    // [★] 엔티티를 DTO로 변환해주는 헬퍼 메서드
    public static ClockInOutResponseDto fromEntity(Attendance entity) {
        return ClockInOutResponseDto.builder()
                .attendanceId(entity.getId())
                .date(entity.getDate())
                .clockInTime(entity.getClockInTime())
                .clockOutTime(entity.getClockOutTime())
                .status(entity.getStatus())
                .build();
    }
}
