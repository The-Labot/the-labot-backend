package com.example.the_labot_backend.reports.dto;

import com.example.the_labot_backend.reports.Report;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportListResponse {

    private Long id;               // 작업일보 ID
    private String workType;       // 공종명
    private String writerName;     // 작성자 이름
    private LocalDateTime createdAt; // 작성일시
    private int manpowerCount;     // 근로자 인원 수
    private String specialNote;    // 특이사항

    public static ReportListResponse from(Report report) {
        return ReportListResponse.builder()
                .id(report.getId())
                .workType(report.getWorkType())
                .writerName(report.getWriter().getName())
                .createdAt(report.getCreatedAt())
                .manpowerCount(report.getManpowerCount())
                .specialNote(report.getSpecialNote())
                .build();
    }
}
