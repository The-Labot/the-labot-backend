package com.example.the_labot_backend.map.controller;

import com.example.the_labot_backend.files.dto.FileResponse;
import com.example.the_labot_backend.map.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/worker/map") // [★ 1. URL 경로 수정 완료]
@RequiredArgsConstructor
public class WorkerMapController {

    private final MapService mapService;
    /**
     * [신규] 근로자가 "지도 보기"를 눌렀을 때
     * 본인이 속한 현장의 지도 이미지 URL을 반환
     */
    @GetMapping
    public ResponseEntity<?> getSiteMap() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

//        if (user.getSite() == null) {
//            return ResponseEntity.status(400).body(Map.of(
//                    "status", 400, "message", "배정된 현장이 없습니다."));
//        }

        List<FileResponse> response = mapService.getMapByUser("SITE_MAP",userId);

//        if (mapUrl == null || mapUrl.isEmpty()) {
//            return ResponseEntity.status(404).body(Map.of(
//                    "status", 404, "message", "현장 지도가 등록되지 않았습니다."));
//        }

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "siteMapUrl", response
        ));
    }
}
