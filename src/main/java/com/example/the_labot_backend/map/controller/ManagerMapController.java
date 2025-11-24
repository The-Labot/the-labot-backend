package com.example.the_labot_backend.map.controller;

import com.example.the_labot_backend.files.dto.FileResponse;
import com.example.the_labot_backend.files.service.FileService;
import com.example.the_labot_backend.map.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manager/map") // [★ 1. URL 경로 수정 완료]
@RequiredArgsConstructor
public class ManagerMapController {

    private final FileService fileService;
    private final MapService mapService;

    // 지도 등록
    @PostMapping()
    public ResponseEntity<?> uploadMap(@RequestParam(required = false) List<MultipartFile> files) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        mapService.uploadMap(userId, files);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "지도 등록 성공"
        ));
    }

    // 지도 조회
    @GetMapping
    public ResponseEntity<?> getSiteMap() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        List<FileResponse> response = mapService.getMapByUser(userId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "지도 조회 성공",
                "siteMapUrl", response
        ));
    }

    // 지도 삭제
    @DeleteMapping()
    public ResponseEntity<?> deleteMap() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        mapService.deleteMap(userId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "지도 삭제 성공"
        ));
    }

}