package com.example.the_labot_backend.notices.controller;

import com.example.the_labot_backend.notices.NoticeService;
import com.example.the_labot_backend.notices.dto.*;
import com.example.the_labot_backend.users.User;
import com.example.the_labot_backend.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager/notices")
public class ManagerNoticeController {

    private final NoticeService noticeService;

    // 공지사항 목록 조회 (현장별)
    @GetMapping
    public ResponseEntity<?> getNoticeList() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        List<NoticeListResponse> response = noticeService.getNoticeList(userId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "공지사항 목록 조회 성공",
                "data", response
        ));
    }

    // 공지사항 상세 조회
    @GetMapping("/{noticeId}")
    public ResponseEntity<?> getNoticeDetail(@PathVariable Long noticeId) {
        NoticeDetailResponse response = noticeService.getNoticeDetail(noticeId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "공지사항 상세 조회 성공",
                "data", response
        ));
    }

    // 공지사항 작성
    @PostMapping
    public ResponseEntity<?> createNotice(@RequestBody NoticeRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long writerId = Long.parseLong(auth.getName());

        NoticeResponse response = noticeService.createNotice(writerId, request);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "공지사항 작성 성공",
                "data", response
        ));
    }

    // 공지사항 수정
    @PutMapping("/{noticeId}")
    public ResponseEntity<?> updateNotice(
            @PathVariable Long noticeId,
            @RequestBody NoticeUpdateRequest request
    ) {
        NoticeResponse response = noticeService.updateNotice(noticeId, request);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "공지사항 수정 성공",
                "data", response
        ));
    }

    // 공지사항 삭제
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<?> deleteNotice(@PathVariable Long noticeId) {
        noticeService.deleteNotice(noticeId);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "공지사항 삭제 성공"
        ));
    }
}