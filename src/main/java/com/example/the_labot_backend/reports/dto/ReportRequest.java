package com.example.the_labot_backend.reports.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportRequest {

    private String workType;        // 공종명
    private String todayWork;       // 금일 작업사항
    private String tomorrowPlan;    // 명일 예정사항
    private int manpowerCount;      // 투입 인원
    private String materialStatus;  // 자재 현황
    private String equipmentStatus; // 장비 현황
    private String specialNote;     // 특이사항
}
