package com.example.the_labot_backend.hazards.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HazardListResponse {
    private Long id;             // 신고 ID
    private String hazardType;   // 위험 유형
    private String reporter;     // 신고자 이름
    private String location;     // 위치
    private boolean isUrgent;    // 긴급 여부
    private String status;       // 상태
    private String reportedAt;   // 신고 시간 (예: "5분 전", "1시간 전")
}
