package com.example.the_labot_backend.authuser.controller;


import com.example.the_labot_backend.authuser.dto.ManagerListResponse;
import com.example.the_labot_backend.authuser.service.ManagerTableService;
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
@RequestMapping("/api/manager/co-workers") // 동료 관리자 조회
@RequiredArgsConstructor
public class ManagerTableController {
    private final ManagerTableService managerService;

    /**
     * [GET] 나와 같은 현장의 관리자 목록 조회
     */
    @GetMapping
    public ResponseEntity<?> getCoManagers() {
        // 1. 현재 로그인한 사용자 ID 추출
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = Long.parseLong(auth.getName());

        // 2. 서비스 호출
        List<ManagerListResponse> list = managerService.getCoManagers(currentUserId);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "동료 관리자 목록 조회 성공",
                "data", list
        ));
    }
}
