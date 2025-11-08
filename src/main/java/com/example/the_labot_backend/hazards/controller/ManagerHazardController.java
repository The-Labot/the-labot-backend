package com.example.the_labot_backend.hazards.controller;


import com.example.the_labot_backend.hazards.HazardService;
import com.example.the_labot_backend.hazards.dto.HazardDetailResponse;
import com.example.the_labot_backend.hazards.dto.HazardListResponse;
import com.example.the_labot_backend.hazards.entity.Hazard;
import com.example.the_labot_backend.users.dto.HazardStatusUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager/hazards") // 관리자/현장관리자용 엔드포인트 예시
@PreAuthorize("hasRole('MANAGER')")
public class ManagerHazardController {

    private final HazardService hazardService;

    // 🔎 목록 조회
    @GetMapping
    public ResponseEntity<?> getHazardList() {
        List<HazardListResponse> list = hazardService.getHazardList();

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "위험요소 신고 목록 조회 성공",
                "data", list
        ));
    }

    // 위험요소 신고 상세 조회
    @GetMapping("/{hazardId}")
    public ResponseEntity<?> getHazardDetail(@PathVariable Long hazardId) {
        HazardDetailResponse response = hazardService.getHazardDetail(hazardId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "위험요소 신고 상세 조회 성공",
                "data", response
        ));
    }

    // 위험요소 신고 상태 변경
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody HazardStatusUpdateRequest request
    ) {
        Hazard updated = hazardService.updateStatus(id, request.getStatus());
        return ResponseEntity.ok().body(
                java.util.Map.of(
                        "status", 200,
                        "message", "위험요소 상태 수정 성공",
                        "data", java.util.Map.of(
                                "hazardId", updated.getId(),
                                "status", updated.getStatus()
                        )
                )
        );
    }

    // 위험요소 신고 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHazard(@PathVariable Long id) {
        hazardService.deleteHazard(id);
        return ResponseEntity.ok().body(
                java.util.Map.of(
                        "status", 200,
                        "message", "위험요소 삭제 성공"
                )
        );
    }

}
