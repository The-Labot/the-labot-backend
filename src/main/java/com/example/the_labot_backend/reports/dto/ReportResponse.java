package com.example.the_labot_backend.reports.dto;

import com.example.the_labot_backend.reports.entity.Report;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportResponse {

    private Long id;
    private String writerName;
    private String siteName;
    private LocalDateTime createdAt;
    private String workType;
    private String todayWork;
    private String tomorrowPlan;
    private int manpowerCount;
    private String materialStatus;
    private String equipmentStatus;
    private String specialNote;

    public static ReportResponse from(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .writerName(report.getWriter().getName())
                .siteName(report.getSite().getProjectName())
                .createdAt(report.getCreatedAt())
                .workType(report.getWorkType())
                .todayWork(report.getTodayWork())
                .tomorrowPlan(report.getTomorrowPlan())
                .manpowerCount(report.getManpowerCount())
                .materialStatus(report.getMaterialStatus())
                .equipmentStatus(report.getEquipmentStatus())
                .specialNote(report.getSpecialNote())
                .build();
    }
}
