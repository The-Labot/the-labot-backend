package com.example.the_labot_backend.hazards.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HazardDetailResponse {
    private Long id;               // 신고 ID
    private String hazardType;     // 위험 유형
    private String reporter;       // 신고자 이름
    private String location;       // 위치
    private String description;    // 설명
    private String fileUrl;        // 이미지 URL
    private boolean isUrgent;      // 긴급 여부
    private String status;         // 상태 (예: PENDING, APPROVED 등)
    private String reportedAt;     // 신고 시각 (ISO 형식)
}

