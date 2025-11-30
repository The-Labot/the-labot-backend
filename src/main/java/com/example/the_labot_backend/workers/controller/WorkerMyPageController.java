package com.example.the_labot_backend.workers.controller;



import com.example.the_labot_backend.workers.dto.WorkerMyPageResponse;
import com.example.the_labot_backend.workers.dto.WorkerMyPageUpdateRequest;
import com.example.the_labot_backend.workers.service.WorkerMyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/worker")
@RequiredArgsConstructor
public class WorkerMyPageController {

    private final WorkerMyPageService workerMyPageService;

    @GetMapping("/mypage")
    public ResponseEntity<WorkerMyPageResponse> getMyPage() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(workerMyPageService.getMyPageInfo(userId));
    }

    // 2. [★ 신규 추가] 마이페이지 정보 수정
    // URL: PUT /api/worker/mypage
    @PatchMapping("/mypage")
    public ResponseEntity<?> updateMyPage(@RequestBody WorkerMyPageUpdateRequest request) {

        Long userId = getCurrentUserId();

        // 서비스 호출
        workerMyPageService.updateMyPageInfo(userId, request);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "정보 수정이 완료되었습니다."
        ));
    }

    @GetMapping("/files/download/{fileId}")
    public ResponseEntity<Resource> downloadMyFile(@PathVariable Long fileId) {
        Long userId = getCurrentUserId();

        // 서비스 내부 클래스 DTO 사용
        WorkerMyPageService.FileDownloadDto fileData = workerMyPageService.downloadFile(userId, fileId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileData.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileData.getOriginalFileName() + "\"")
                .body(fileData.getResource());
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("로그인 정보가 없습니다.");
        }
        return Long.parseLong(auth.getName());
    }


}
