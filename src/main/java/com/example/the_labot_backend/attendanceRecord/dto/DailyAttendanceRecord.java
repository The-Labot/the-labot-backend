package com.example.the_labot_backend.attendanceRecord.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DailyAttendanceRecord {

    private String date;

    private String clockIn;
    private String clockOut;

    private double totalWork;
    private double basicWork;
    private double extendedWork;
    private double overtimeWork;
    private double nightWork;
    private double holidayWork;

    private long unitPrice;
    private double manHour;
}