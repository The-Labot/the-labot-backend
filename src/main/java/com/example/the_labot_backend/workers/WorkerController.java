package com.example.the_labot_backend.workers;

import com.example.the_labot_backend.workers.dto.WorkerCreateRequest;
import com.example.the_labot_backend.workers.dto.WorkerUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/manager/workers")
@RequiredArgsConstructor
public class WorkerController {

    private final WorkerService workerService;

    // 근로자 등록
    @PostMapping
    public ResponseEntity<?> createWorker(
            @RequestBody WorkerCreateRequest request
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        workerService.createWorker(userId, request);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "현장근로자 계정 생성 완료")
        );
    }

    // 근로자 목록 조회
    @GetMapping
    public ResponseEntity<?> getAllWorkers() {
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "근로자 목록 조회 성공",
                "data", workerService.getWorkers()
        ));
    }

    // 근로자 상세 조회
    @GetMapping("/{workerId}")
    public ResponseEntity<?> getWorkerDetail(@PathVariable Long workerId) {
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "근로자 상세 정보 조회 성공",
                "data", workerService.getWorkerDetail(workerId)
        ));
    }

    // 근로자 정보 수정
    @PutMapping("/{workerId}")
    public ResponseEntity<?> updateWorker(@PathVariable Long workerId,
                                          @RequestBody WorkerUpdateRequest dto) {
        workerService.updateWorker(workerId, dto);
        return ResponseEntity.ok(Map.of("status", 200, "message", "근로자 정보 수정 완료"));
    }
}

