package com.example.the_labot_backend.admins;

import com.example.the_labot_backend.admins.dto.ManagerCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/sites")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService service;

    // 현장관리자 생성
    @PostMapping("/{siteId}/manager")
    public ResponseEntity<?> createManager(
            @PathVariable Long siteId,
            @RequestBody ManagerCreateRequest request) {
        service.createManager(siteId, request);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "현장관리자 회원가입 성공"
        ));
    }
}
