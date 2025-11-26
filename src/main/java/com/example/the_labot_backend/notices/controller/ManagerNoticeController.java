package com.example.the_labot_backend.notices.controller;

import com.example.the_labot_backend.notices.dto.NoticeCreateForm;
import com.example.the_labot_backend.notices.dto.NoticeDetailResponse;
import com.example.the_labot_backend.notices.dto.NoticeListResponse;
import com.example.the_labot_backend.notices.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manager/notices")
public class ManagerNoticeController {

    private final NoticeService noticeService;

    // 공지사항 작성
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createNotice(
            @ModelAttribute NoticeCreateForm form,
            @RequestPart(required = false) List<MultipartFile> files
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        noticeService.createNotice(userId, form, files);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "공지사항이 등록되었습니다."
        ));
    }

    // 현장별 공지사항 목록 조회 (현장관리자)
    @GetMapping
    public ResponseEntity<?> getNoticeList(@RequestParam(required = false) String title) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        List<NoticeListResponse> response = noticeService.getNoticesByUser(userId, title);
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

    // 공지사항 수정
    @PutMapping(value = "/{noticeId}", consumes = "multipart/form-data")
    public ResponseEntity<?> updateNotice(
            @PathVariable Long noticeId,
            @ModelAttribute NoticeCreateForm form,
            @RequestParam(required = false) List<MultipartFile> files
    ) {
        NoticeDetailResponse response = noticeService.updateNotice(
                noticeId, form, files
        );

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