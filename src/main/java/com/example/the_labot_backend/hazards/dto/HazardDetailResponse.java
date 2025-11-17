package com.example.the_labot_backend.hazards.dto;

import com.example.the_labot_backend.files.entity.File;
import com.example.the_labot_backend.hazards.entity.Hazard;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter


public class HazardDetailResponse {
    private final Long id;               // 신고 ID
    private final String hazardType;     // 위험 유형
    private final String reporter;       // 신고자 이름
    private final String location;       // 위치
    private final String description;    // 설명
    private final boolean isUrgent;      // 긴급 여부
    private final String status;         // 상태 (예: PENDING, APPROVED 등)
    private final LocalDateTime reportedAt;     // 신고 시각 (ISO 형식)
    private final List<HazardDetailResponse.FileResponse> files;

    public HazardDetailResponse(Hazard hazard, List<File> files) {
        this.id = hazard.getId();
        this.hazardType = hazard.getHazardType();
        this.reporter = hazard.getReporter().getName();
        this.location = hazard.getLocation();
        this.description = hazard.getDescription();
        this.isUrgent = hazard.isUrgent();
        this.status = hazard.getStatus().name();
        this.reportedAt = hazard.getReportedAt();
        this.files = files.stream().map(FileResponse::new).toList();
    }

    @Getter
    static class FileResponse {
        private Long id;
        private String fileUrl;
        private String originalFileName;

        public FileResponse(File file) {
            this.id = file.getId();
            this.fileUrl = file.getFileUrl();
            this.originalFileName = file.getOriginalFileName();
        }
    }
}

