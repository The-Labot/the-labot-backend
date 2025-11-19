package com.example.the_labot_backend.hazards.controller;

import com.example.the_labot_backend.hazards.service.HazardService;
import com.example.the_labot_backend.hazards.dto.HazardCreateRequest;
import com.example.the_labot_backend.hazards.entity.HazardStatus;
import com.example.the_labot_backend.notices.entity.NoticeCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/worker/hazards")
//@PreAuthorize("hasRole('WORKER')")  // 현장근로자만 접근 가능
public class WorkerHazardController {

    private final HazardService hazardService;

    // 🟢 위험요소 신고 등록
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createHazard(
            @RequestParam String hazardType,
            @RequestParam String location,
            @RequestParam String description,
            @RequestParam boolean urgent,
            @RequestParam HazardStatus status,
            @RequestParam(required = false) List<MultipartFile> files
    ) {
        //Jwt에서 인증된 사용자 ID 가져오기

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName()); // 식별자로 userId를 사용

        hazardService.createHazard(hazardType, location, description, urgent, status, files, userId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "위험요소 신고가 등록되었습니다."
        ));
    }


}
