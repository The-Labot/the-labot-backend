package com.example.the_labot_backend.notices.worker.controller;

import com.example.the_labot_backend.notices.worker.dto.WorkerNoticeDetailDto;
import com.example.the_labot_backend.notices.worker.dto.WorkerNoticeListDto;
import com.example.the_labot_backend.notices.worker.service.WorkerNoticeService;
// ▼▼▼ [★ 수정된 부분] User와 UserRepository 경로 변경 ▼▼▼
import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository; // [테스트용]
// ▲▲▲ [★ 수정된 부분] ▲▲▲
import jakarta.persistence.EntityNotFoundException; // [테스트용]
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/worker/notices") // 근로자용 API
@RequiredArgsConstructor
public class WorkerNoticeController {
    private final WorkerNoticeService workerNoticeService;
    private final UserRepository userRepository;


    /**
     * 목록 조회 API
     * GET /api/worker/notices?category=SAFETY&yearMonth=2024-12
     */
    @GetMapping
    public ResponseEntity<List<WorkerNoticeListDto>> getNoticeList(
            @RequestParam(defaultValue = "ALL") String category,
            @RequestParam(required = false) String yearMonth) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();


        Long userId = Long.parseLong(auth.getName()); // 2. ID(PK) 추출


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다. ID: " + userId));// 3. DB에서 진짜 User 조회

        List<WorkerNoticeListDto> notices = workerNoticeService.getNoticesForWorker(user, category, yearMonth);
        return ResponseEntity.ok(notices);
    }

    /**
     * 상세 조회 API
     */
    @GetMapping("/{noticeId}")
    public ResponseEntity<WorkerNoticeDetailDto> getNoticeDetail(
            @PathVariable Long noticeId) {

        WorkerNoticeDetailDto noticeDetail = workerNoticeService.getNoticeDetailForWorker(noticeId);
        return ResponseEntity.ok(noticeDetail);
    }
}
