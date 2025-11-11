package com.example.the_labot_backend.sites.controller;

import com.example.the_labot_backend.sites.SiteService;
import com.example.the_labot_backend.sites.dto.SiteListResponse;
import com.example.the_labot_backend.sites.dto.SiteRequest;
import com.example.the_labot_backend.sites.dto.SiteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/sites")
@RequiredArgsConstructor
public class AdminSiteController {
    private final SiteService siteService;

    // 현장 등록
    @PostMapping
    public ResponseEntity<?> createSite(@RequestBody SiteRequest request) {
        siteService.createSite(request);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "현장 등록 성공"
        ));
    }

    // 현장 목록 조회 (보류)
    @GetMapping
    public ResponseEntity<?> getAllSite() {
        List<SiteListResponse> response = siteService.getAllSites();
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "현장 상세 조회 성공",
                "data", response
        ));
    }

    // 현장 상세 조회
    @GetMapping("/{siteId}")
    public ResponseEntity<?> getSite(@PathVariable Long siteId) {
        SiteResponse response = siteService.getSiteBySiteId(siteId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "현장 상세 조회 성공",
                "data", response
        ));
    }

    // 현장 수정
    @PutMapping("/{siteId}")
    public ResponseEntity<?> updateSite(
            @PathVariable Long siteId,
            @RequestBody SiteRequest request) {
        SiteResponse response = siteService.updateSite(siteId,request);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "현장 수정 성공",
                "data", response
        ));
    }

    // 현장 삭제
    @DeleteMapping("/{siteId}")
    public ResponseEntity<?> deleteSite(@PathVariable Long siteId) {
        siteService.deleteSite(siteId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "현장 삭제 성공"
        ));
    }

}
