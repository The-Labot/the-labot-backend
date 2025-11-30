package com.example.the_labot_backend.reports.controller;

import com.example.the_labot_backend.reports.dto.DailyReportListResponse;
import com.example.the_labot_backend.reports.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private final ReportService reportService;

    @GetMapping("/{siteId}/today")
    public ResponseEntity<?> getTodayReports(@PathVariable Long siteId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        List<DailyReportListResponse> response = reportService.getTodayReports(siteId,userId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "오늘 작업일보 목록 조회 성공",
                "data", response
        ));
    }
}