package com.example.the_labot_backend.hazards;


import com.example.the_labot_backend.hazards.dto.HazardDetailResponse;
import com.example.the_labot_backend.hazards.dto.HazardListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager/hazards") // 관리자/현장관리자용 엔드포인트 예시
public class HazardReportController {

    private final HazardReportService hazardReportService;

    // 🔎 목록 조회
    @GetMapping
    public ResponseEntity<?> getHazardList() {
        List<HazardListResponse> list = hazardReportService.getHazardList();

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "위험요소 신고 목록 조회 성공",
                "data", list
        ));
    }

    // 위험요소 신고 상세 조회
    @GetMapping("/{hazardId}")
    public ResponseEntity<?> getHazardDetail(@PathVariable Long hazardId) {
        HazardDetailResponse response = hazardReportService.getHazardDetail(hazardId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "위험요소 신고 상세 조회 성공",
                "data", response
        ));
    }
}
