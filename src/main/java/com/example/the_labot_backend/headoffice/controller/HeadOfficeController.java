package com.example.the_labot_backend.headoffice.controller;

import com.example.the_labot_backend.headoffice.dto.HeadOfficeCheckResponse;
import com.example.the_labot_backend.headoffice.dto.HeadOfficeRequest;
import com.example.the_labot_backend.headoffice.dto.HeadOfficeResponse;
import com.example.the_labot_backend.headoffice.dto.SecretCodeRequest;
import com.example.the_labot_backend.headoffice.service.HeadOfficeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/head-office")
@RequiredArgsConstructor
public class HeadOfficeController {
    private final HeadOfficeService headOfficeService;

    // 본사 등록
    @PostMapping
    public ResponseEntity<?> create(@RequestBody HeadOfficeRequest request) {
        HeadOfficeResponse response = headOfficeService.createHeadOffice(request);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "본사 생성 성공",
                "data", response
        ));
    }

    // 본사코드로 본사 선택
    @PostMapping("/select")
    public ResponseEntity<?> checkHeadOffice(@RequestBody SecretCodeRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        HeadOfficeCheckResponse response = headOfficeService.checkHeadOffice(userId, request.getSecretCode());

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "본사 조회 성공",
                "data", response
        ));
    }

    // 본사 존재 여부 확인 (True/False)
    @GetMapping("/exists")
    public ResponseEntity<?> hasHeadOffice() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        boolean exists = headOfficeService.hasHeadOffice(userId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "본사 존재 여부 확인 성공",
                "data", Map.of("hasHeadOffice", exists)
        ));
    }

    // 본사 상세 조회
    @GetMapping
    public ResponseEntity<?> detail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        HeadOfficeResponse response = headOfficeService.getHeadOffice(userId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "본사 상세 조회 성공",
                "data", response
        ));
    }

    // 본사 수정
    @PutMapping
    public ResponseEntity<?> update(@RequestBody HeadOfficeRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        HeadOfficeResponse response = headOfficeService.updateHeadOffice(userId, request);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "본사 수정 성공",
                "data", response
        ));
    }
}
