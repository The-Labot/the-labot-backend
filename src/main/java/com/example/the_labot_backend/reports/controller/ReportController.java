package com.example.the_labot_backend.reports.controller;

import com.example.the_labot_backend.reports.dto.ReportListResponse;
import com.example.the_labot_backend.reports.dto.ReportRequest;
import com.example.the_labot_backend.reports.dto.ReportResponse;
import com.example.the_labot_backend.reports.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manager/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // 작업일보 등록
    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody ReportRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        ReportResponse response = reportService.createReport(userId, request);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "작업일보 등록 성공",
                "data", response
        ));
    }

    // 현장별 작업일보 목록 조회
    @GetMapping
    public ResponseEntity<?> getReportList() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        List<ReportListResponse> responses = reportService.getReportsByUser(userId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "작업일보 목록 조회 성공",
                "data", responses
        ));
    }

    // 작업일보 상세조회
    @GetMapping("/{reportId}")
    public ResponseEntity<?> getReport(@PathVariable Long reportId) {
        ReportResponse response = reportService.getReport(reportId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "작업일보 상세 조회 성공",
                "data", response
        ));
    }

    // 작업일보 수정
    @PutMapping("/{reportId}")
    public ResponseEntity<?> updateReport(@PathVariable Long reportId,
                                          @RequestBody ReportRequest request) {
        ReportResponse response = reportService.updateReport(reportId, request);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "작업일보 수정 성공",
                "data", response
        ));
    }

    // 작업일보 삭제
    @DeleteMapping("/{reportId}")
    public ResponseEntity<?> deleteReport(@PathVariable Long reportId) {
        reportService.deleteReport(reportId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "작업일보 삭제 성공"
        ));
    }
}