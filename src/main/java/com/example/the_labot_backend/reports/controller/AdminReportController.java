package com.example.the_labot_backend.reports.controller;

import com.example.the_labot_backend.reports.dto.DailyReportListResponse;
import com.example.the_labot_backend.reports.dto.ReportDetailResponse;
import com.example.the_labot_backend.reports.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    private final ReportService reportService;

    @GetMapping("/sites/{siteId}")
    public ResponseEntity<?> getTodayReports(
            @PathVariable Long siteId,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day
    ) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        List<DailyReportListResponse> response = reportService.getReports(siteId,userId,year,month,day);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "오늘 작업일보 목록 조회 성공",
                "data", response
        ));
    }

    // 작업일보 상세조회
    @GetMapping("/{reportId}")
    public ResponseEntity<?> getReport(@PathVariable Long reportId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        ReportDetailResponse response = reportService.getReportDetail(userId,reportId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "작업일보 상세 조회 성공",
                "data", response
        ));
    }
}