package com.example.the_labot_backend.admins.controller;

import com.example.the_labot_backend.admins.dto.AdminWorkerListResponse;
import com.example.the_labot_backend.admins.service.AdminWorkerService;
import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
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
@RequestMapping("/api/admin/workers") // 본사 관리자용 경로
@RequiredArgsConstructor
public class AdminWorkerController {

    private final AdminWorkerService adminWorkerService;
    private final UserRepository userRepository;

    // 통합 근로자 목록 조회 (GET)
    @GetMapping
    public ResponseEntity<?> getAllWorkers() {

        // 1. 현재 로그인한 관리자 ID 추출
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long adminId = Long.parseLong(auth.getName());

        // 2. 관리자 정보 조회
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("관리자 정보를 찾을 수 없습니다."));

        // 3. 서비스 호출 (본사 산하 모든 근로자 가져오기)
        List<AdminWorkerListResponse> workerList = adminWorkerService.getAllWorkersByHeadOffice(admin);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "본사 통합 근로자 목록 조회 성공",
                "count", workerList.size(),
                "data", workerList
        ));
    }
}
