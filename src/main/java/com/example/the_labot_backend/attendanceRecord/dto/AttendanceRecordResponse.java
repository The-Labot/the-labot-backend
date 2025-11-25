package com.example.the_labot_backend.attendanceRecord.dto;

import com.example.the_labot_backend.attendanceRecord.entity.AttendanceRecord;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class AttendanceRecordResponse {
    private Long id;
    private LocalDate workDate;
    private Double actualTotalWork;       // 실제 총근로
    private Double actualBasicWork;       // 실제 기본근로
    private Double actualExtendedWork;    // 실제 연장근로
    private Double actualOvertimeWork;    // 실제 초과근로
    private Double actualNightWork;       // 실제 야간근로
    private Double actualHolidayWork;     // 실제 휴일근로
    private Long unitPrice;   // 기준단가
    private Double manHour;    // 공수

    public static AttendanceRecordResponse from(AttendanceRecord record) {
        return AttendanceRecordResponse.builder()
                .id(record.getId())
                .workDate(record.getWorkDate())
                .actualTotalWork(record.getActualTotalWork())
                .actualBasicWork(record.getActualBasicWork())
                .actualExtendedWork(record.getActualExtendedWork())
                .actualOvertimeWork(record.getActualOvertimeWork())
                .actualNightWork(record.getActualNightWork())
                .actualHolidayWork(record.getActualHolidayWork())
                .unitPrice(record.getUnitPrice())
                .manHour(record.getManHour())
                .build();
    }
}
