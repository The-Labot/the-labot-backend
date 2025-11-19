package com.example.the_labot_backend.map.controller;

import com.example.the_labot_backend.files.dto.FileResponse;
import com.example.the_labot_backend.files.service.FileService;
import com.example.the_labot_backend.map.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/sites") // [★ 1. URL 경로 수정 완료]
@RequiredArgsConstructor
public class AdminMapController {

    private final FileService fileService;
    private final MapService mapService;

    @GetMapping("/{siteId}/map")
    public ResponseEntity<?> getSiteMap(@PathVariable Long siteId) {

        List<FileResponse> response = mapService.getMapBySite("SITE_MAP", siteId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "siteMapUrl", response
        ));
    }
}
