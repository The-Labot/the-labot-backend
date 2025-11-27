package com.example.the_labot_backend.workers.controller;

import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.files.entity.File;
import com.example.the_labot_backend.files.repository.FileRepository;
import com.example.the_labot_backend.workers.dto.WorkerMyPageResponse;
import com.example.the_labot_backend.workers.entity.Worker;
import com.example.the_labot_backend.workers.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/worker")
@RequiredArgsConstructor
public class WorkerMyPageController {
    private final UserRepository userRepository;
    private final WorkerRepository workerRepository;
    private final FileRepository fileRepository;

    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    // ==========================================
    // 1. 마이페이지 정보 조회
    // URL: GET /api/worker/mypage
    // ==========================================
    @GetMapping("/mypage")
    public ResponseEntity<WorkerMyPageResponse> getMyPage() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("로그인 정보가 없습니다.");
        }
        Long userId = Long.parseLong(auth.getName());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

        // 2. Worker 조회
        Worker worker = user.getWorker();
        // 예외 처리: 만약 근로자가 아닌 일반 유저가 들어왔을 경우 worker가 null일 수 있음
        if (worker == null) {
            throw new RuntimeException("해당 계정은 근로자 정보가 등록되지 않았습니다.");
        }



        // (1) 근로계약서 조회
        List<File> contractFiles = fileRepository.findByTargetTypeAndTargetId("WORKER_CONTRACT", worker.getId());
        Long contractId = contractFiles.isEmpty() ? null : contractFiles.get(0).getId();

        // (2) 급여명세서 조회
        List<File> payrollFiles = fileRepository.findByTargetTypeAndTargetId("WORKER_PAYROLL", worker.getId());
        Long payrollId = payrollFiles.isEmpty() ? null : payrollFiles.get(0).getId();

        // (3) 자격증 조회
        List<File> licenseFiles = fileRepository.findByTargetTypeAndTargetId("WORKER_LICENSE", worker.getId());
        Long certId = licenseFiles.isEmpty() ? null : licenseFiles.get(0).getId();



        // 5. DTO 변환
        WorkerMyPageResponse response = WorkerMyPageResponse.from(user, worker, contractId, payrollId, certId);

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // 2. 근로자 본인 파일 다운로드
    // URL: GET /api/worker/files/download/{fileId}
    // ==========================================
    @GetMapping("/files/download/{fileId}")
    public ResponseEntity<Resource> downloadMyFile(
            @PathVariable Long fileId

    ) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getName() == null) {
                throw new RuntimeException("로그인 정보가 없습니다.");
            }
            Long userId = Long.parseLong(auth.getName());

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

            Worker worker = user.getWorker();
            if (worker == null) throw new RuntimeException("근로자 정보 없음");

            // 파일 조회
            File fileEntity = fileRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("파일 없음"));

            if (!fileEntity.getTargetId().equals(worker.getId()) ||
                    !fileEntity.getTargetType().startsWith("WORKER")) {
                throw new RuntimeException("본인의 파일만 다운로드할 수 있습니다.");
            }

            Path filePath = Paths.get(UPLOAD_DIR + fileEntity.getStoredFileName());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) throw new RuntimeException("파일 없음");

            String contentType = fileEntity.getContentType() != null ? fileEntity.getContentType() : "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getOriginalFileName() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            throw new RuntimeException("파일 경로 오류", e);
        }
    }


}
