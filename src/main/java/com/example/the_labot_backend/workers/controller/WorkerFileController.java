package com.example.the_labot_backend.workers.controller;

import com.example.the_labot_backend.files.dto.FileResponse;
import com.example.the_labot_backend.workers.service.WorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/manager/workers/files") // [★] URL 변경: workers 하위로 이동
@RequiredArgsConstructor
public class WorkerFileController {
    // WorkerService가 파일 검증 로직도 담당하도록 합니다.
    private final WorkerService workerService;

    /**
     * 근로자 관련 파일 개별 상세 조회 (보안 검증 포함)
     * 앱에서 근로자 상세 -> 파일 클릭 시 호출
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<FileResponse> getWorkerFileDetail(@PathVariable Long fileId) {

        // 1. 관리자(요청자) ID 추출
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long managerId = Long.parseLong(auth.getName());

        // 2. 서비스 호출 (검증 후 DTO 반환)
        FileResponse fileResponse = workerService.getWorkerFileWithValidation(managerId, fileId);

        return ResponseEntity.ok(fileResponse);
    }
}
