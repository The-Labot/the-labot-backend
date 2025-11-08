package com.example.the_labot_backend.hazards;


import com.example.the_labot_backend.hazards.dto.HazardDetailResponse;
import com.example.the_labot_backend.hazards.dto.HazardListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HazardReportService {

    private final HazardReportRepository hazardReportRepository;

    // 목록 조회
    public List<HazardListResponse> getHazardList() {
        List<HazardReport> reports = hazardReportRepository.findAll();

        return reports.stream()
                .map(hazard -> HazardListResponse.builder()
                        .id(hazard.getId())
                        .hazardType(hazard.getHazardType())
                        .reporter(hazard.getReporter().getName())
                        .location(hazard.getLocation())
                        .isUrgent(hazard.isUrgent())
                        .status(hazard.getStatus().name())
                        .reportedAt(formatTimeAgo(hazard.getReportedAt()))
                        .build())
                .toList();
    }

    // 상세조회
    public HazardDetailResponse getHazardDetail(Long hazardId) {
        HazardReport hazard = hazardReportRepository.findById(hazardId)
                .orElseThrow(() -> new RuntimeException("해당 위험요소 신고를 찾을 수 없습니다."));

        return HazardDetailResponse.builder()
                .id(hazard.getId())
                .hazardType(hazard.getHazardType())
                .reporter(hazard.getReporter().getName())
                .location(hazard.getLocation())
                .description(hazard.getDescription())
                .fileUrl(hazard.getFileUrl())
                .isUrgent(hazard.isUrgent())
                .status(hazard.getStatus().name())
                .reportedAt(hazard.getReportedAt()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
                .build();
    }

    private String formatTimeAgo(LocalDateTime reportedAt) {
        Duration duration = Duration.between(reportedAt, LocalDateTime.now());
        long minutes = duration.toMinutes();
        long hours = duration.toHours();

        if (minutes < 1) return "방금 전";
        if (minutes < 60) return minutes + "분 전";
        if (hours < 24) return hours + "시간 전";
        return reportedAt.toLocalDate().toString(); // 하루 이상이면 날짜 출력
    }
}
