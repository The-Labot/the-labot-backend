package com.example.the_labot_backend.dashboard.controller;

import com.example.the_labot_backend.dashboard.dto.DashboardResponse;
import com.example.the_labot_backend.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    // =================================================================================
    // 1. [현장소장용] 내 현장 대시보드 조회
    // URL: GET /api/manager/dashboard
    // =================================================================================
    @GetMapping("/api/manager/dashboard")
    @PreAuthorize("hasRole('MANAGER')") // 현장관리자만 접근 가능
    public ResponseEntity<?> getManagerDashboard() {

        // 1. 토큰에서 ID 추출
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long managerId = Long.parseLong(auth.getName());

        // 2. 서비스 호출 (내 현장 조회)
        DashboardResponse response = dashboardService.getDashboardForManager(managerId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "작업 현황 대시보드 조회 성공 (현장관리자)",
                "data", response
        ));
    }

    // =================================================================================
    // 2. [본사관리자용] 특정 현장 대시보드 조회
    // URL: GET /api/admin/sites/{siteId}/dashboard
    // =================================================================================
    @GetMapping("/api/admin/sites/{siteId}/dashboard")
    @PreAuthorize("hasRole('ADMIN')") // 본사관리자만 접근 가능
    public ResponseEntity<?> getAdminSiteDashboard(@PathVariable Long siteId) {

        // 1. 토큰에서 ID 추출
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long adminId = Long.parseLong(auth.getName());

        // 2. 서비스 호출 (특정 현장 조회 + 권한 검증)
        DashboardResponse response = dashboardService.getDashboardForHeadOffice(adminId, siteId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "해당 현장의 대시보드 조회 성공 (본사관리자)",
                "data", response
        ));
    }
}
