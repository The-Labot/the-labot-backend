package com.example.the_labot_backend.headoffice;

import com.example.the_labot_backend.headoffice.dto.HeadOfficeCheckResponse;
import com.example.the_labot_backend.headoffice.dto.SecretCodeRequest;
import com.example.the_labot_backend.headoffice.dto.HeadOfficeRequest;
import com.example.the_labot_backend.headoffice.dto.HeadOfficeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HeadOfficeController {
    private final HeadOfficeService headOfficeService;

    // 본사 등록
    @PostMapping("/auth/head-office")
    public ResponseEntity<?> create(@RequestBody HeadOfficeRequest request) {
        HeadOfficeResponse response = headOfficeService.createHeadOffice(request);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "본사 생성 성공",
                "data", response
        ));
    }

    // 본사코드로 본사조회
    @GetMapping("/auth/head-office/check")
    public ResponseEntity<?> checkHeadOffice(@RequestBody SecretCodeRequest request) {
        HeadOfficeCheckResponse response = headOfficeService.checkHeadOffice(request.getSecretCode());
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "본사 조회 성공",
                "data", response
        ));
    }

    // 본사 상세 조회
    @GetMapping("/admin/head-office")
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
    @PutMapping("/admin/head-office")
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
