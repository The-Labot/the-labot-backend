package com.example.the_labot_backend.reports.dto;

import com.example.the_labot_backend.reports.entity.Report;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportListResponse {

    private Long id;               // 작업일보 ID
    private String workType;       // 공종명
    private String writerName;     // 작성자 이름
    private int workerCount;
    private LocalDateTime createdAt; // 작성일시
    private String todayWork;

    public static ReportListResponse from(Report report) {
        return ReportListResponse.builder()
                .id(report.getId())
                .workType(report.getWorkType())
                .writerName(report.getWriter().getName())
                .createdAt(report.getCreatedAt())
                .todayWork(report.getTodayWork())
                .build();
    }
}
