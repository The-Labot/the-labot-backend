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
@RequestMapping("/api/worker/map")
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

        List<FileResponse> response = mapService.getMapByUser(userId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "지도 조회 성공",
                "siteMapUrl", response
        ));
    }
}
