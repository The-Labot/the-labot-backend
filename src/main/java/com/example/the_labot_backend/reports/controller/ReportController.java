package com.example.the_labot_backend.reports.controller;

import com.example.the_labot_backend.reports.dto.ReportCreateRequest;
import com.example.the_labot_backend.reports.dto.ReportDetailResponse;
import com.example.the_labot_backend.reports.dto.ReportListResponse;
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
    // 작업일보를 등록하면 응답받은 reportId를 통해 바로 파일 등록 api 수행
    @PostMapping
    public ResponseEntity<?> createReport(@RequestBody ReportCreateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        Long response = reportService.createReport(userId, request);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "작업일보 등록 성공",
                "reportId", response
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        ReportDetailResponse response = reportService.getReportDetail(userId,reportId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "작업일보 상세 조회 성공",
                "data", response
        ));
    }

    // 작업일보 수정
    @PutMapping("/{reportId}")
    public ResponseEntity<?> updateReport(@PathVariable Long reportId,
                                          @RequestBody ReportCreateRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        Long response = reportService.updateReport(userId,reportId, request);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "작업일보 수정 성공",
                "reportId", response
        ));
    }

    // 작업일보 삭제
    @DeleteMapping("/{reportId}")
    public ResponseEntity<?> deleteReport(@PathVariable Long reportId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        reportService.deleteReport(userId,reportId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "작업일보 삭제 성공"
        ));
    }
}