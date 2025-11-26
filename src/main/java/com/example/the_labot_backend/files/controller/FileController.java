package com.example.the_labot_backend.files.controller;

import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.files.entity.File;
import com.example.the_labot_backend.files.repository.FileRepository;
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
import java.util.Objects;
@RestController
@RequestMapping("/api/manager/files")
@RequiredArgsConstructor
public class FileController {
    private final FileRepository fileRepository;
    private final WorkerRepository workerRepository; // ★ 검증 위해 추가
    private final UserRepository userRepository; // ★ [추가] DB 조회를 위해 필수!
    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    // GET http://localhost:8080/api/files/download/{fileId}
    @GetMapping("/download/{fileId}") //근로자의 상세에서 파일을 열때 필요함
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long fileId

    ) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getName() == null) {
                throw new RuntimeException("로그인 정보가 없습니다.");
            }
            Long userId = Long.parseLong(auth.getName()); // ID 추출
            User manager = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("관리자 정보를 찾을 수 없습니다."));
            // 1. 파일 정보 조회
            File fileEntity = fileRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("파일을 찾을 수 없습니다."));

            // 2. [★ 보안 검증] 이 파일이 근로자 관련 파일이라면, 권한 체크 수행
            validateWorkerFileAccess(fileEntity, manager);


            // 3. 실제 파일 경로 찾기
            Path filePath = Paths.get(UPLOAD_DIR + fileEntity.getStoredFileName());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                throw new RuntimeException("파일이 디스크에 존재하지 않습니다.");
            }

            // 4. 전송
            String contentType = fileEntity.getContentType();
            if(contentType == null) contentType = "application/octet-stream";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getOriginalFileName() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            throw new RuntimeException("파일 경로 오류", e);
        }
    }

    /**
     * [검증 로직]
     * 파일의 주인이 현재 관리자와 같은 현장인지 확인
     */
    private void validateWorkerFileAccess(File file, User manager) {
        // 1. 파일의 타겟이 'WORKER'로 시작하는지 확인 (WORKER_CONTRACT, WORKER_LICENSE 등)
        if (file.getTargetType() != null && file.getTargetType().startsWith("WORKER")) {

            Long workerId = file.getTargetId();

            // 2. 파일 주인(Worker) 찾기
            Worker worker = workerRepository.findById(workerId)
                    .orElseThrow(() -> new RuntimeException("파일과 연결된 근로자가 존재하지 않습니다."));

            // 3. 관리자의 현장(Site) vs 근로자의 현장(Site) 비교
            Long managerSiteId = manager.getSite().getId();
            Long workerSiteId = worker.getUser().getSite().getId();

            if (!Objects.equals(managerSiteId, workerSiteId)) {
                // 현장이 다르면 접근 금지!
                throw new RuntimeException("해당 근로자의 파일을 볼 권한이 없습니다. (타 현장 관리자 접근 불가)");
            }
        }
        // WORKER 관련 파일이 아니면(공지사항 등) 일단 통과시키거나 별도 로직 추가
    }
}
