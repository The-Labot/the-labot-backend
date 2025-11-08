package com.example.the_labot_backend.hazards.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HazardCreateRequest {
    private String hazardType;   // 위험 유형
    private String location;     // 위치
    private String description;  // 설명
    private String fileUrl;      // 이미지 URL
    private boolean urgent;    // 긴급 여부
}
