package com.example.the_labot_backend.sites.controller;

import com.example.the_labot_backend.sites.dto.DashboardResponse;
import com.example.the_labot_backend.sites.dto.SiteResponse;
import com.example.the_labot_backend.sites.service.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/sites")
@RequiredArgsConstructor
public class AdminSiteController {
    private final SiteService siteService;

    // 현장 등록
    @PostMapping
    public ResponseEntity<?> createSite(
            @RequestParam String siteName,
            @RequestParam String siteAddress,
            @RequestParam String description,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam String constructionType,
            @RequestParam String client,
            @RequestParam String contractor,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) List<MultipartFile> files
    ) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        siteService.createSite(userId,siteName,siteAddress,description,startDate,endDate,constructionType,client,contractor,latitude,longitude,files);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "현장 등록 성공"
        ));
    }

    // 현장 대시보드 조회
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        DashboardResponse response = siteService.getDashboard(userId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "대시보드 조회 성공",
                "data", response
        ));
    }

    // 현장 상세 조회
    @GetMapping("/{siteId}")
    public ResponseEntity<?> getSite(@PathVariable Long siteId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        SiteResponse response = siteService.getSiteDetail(userId, siteId);
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
            @RequestParam String siteName,
            @RequestParam String siteAddress,
            @RequestParam String description,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam String constructionType,
            @RequestParam String client,
            @RequestParam String contractor,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) List<MultipartFile> files) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        SiteResponse response = siteService.updateSite(userId, siteId, siteName,siteAddress,description,startDate,endDate,constructionType,client,contractor,latitude,longitude,files );
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
