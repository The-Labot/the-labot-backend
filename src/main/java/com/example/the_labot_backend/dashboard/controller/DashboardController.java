package com.example.the_labot_backend.dashboard.controller;

import com.example.the_labot_backend.dashboard.dto.DashboardResponse;
import com.example.the_labot_backend.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/manager/dashboard")
@RequiredArgsConstructor

public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<?> getDashboard() {

        // 1. 토큰에서 현재 로그인한 관리자 ID 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        // 2. 서비스 호출
        DashboardResponse response = dashboardService.getDashboard(userId);

        // 3. 응답 반환
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "작업 현황 대시보드 조회 성공",
                "data", response
        ));
    }
}
