package com.example.the_labot_backend.attendance.dto;

import lombok.Data;

@Data
public class ClockInOutRequestDto {
    private Double latitude;  // (앱에서 측정한 현재 위도)
    private Double longitude; // (앱에서 측정한 현재 경도)
}
