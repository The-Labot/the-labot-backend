package com.example.the_labot_backend.workers.controller;

import com.example.the_labot_backend.workers.service.WorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/workers")
@RequiredArgsConstructor
public class AdminWorkersController {

    private final WorkerService workerService;

    @GetMapping("/detail/{workerId}")
    public ResponseEntity<?> getWorkerDetail(@PathVariable Long workerId) {
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "근로자 상세 정보 조회 성공",
                "data", workerService.getWorkerDetail(workerId)
        ));
    }
}
