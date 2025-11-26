package com.example.the_labot_backend.attendanceRecord.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MonthlyAttendanceRecordResponse {

    private double monthlyTotalWork;   // 월 총근로시간
    private double monthlyTotalManHour; // 월 공수

    private List<DailyAttendanceRecord> dailyRecords;
}
