package com.example.the_labot_backend.notices;

import com.example.the_labot_backend.notices.dto.*;
import com.example.the_labot_backend.notices.entity.Notice;
import com.example.the_labot_backend.users.User;
import com.example.the_labot_backend.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional // ??? 공부하기
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    // 현장별 공지사항 목록 조회
    public List<NoticeListResponse> getNoticeList(Long userId) {

        // 해당 User 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // user로 siteId 찾기
        Long siteId = user.getSite().getId();
        
        // 여러개 조회, siteId를 조건으로 조회, 정렬, 정렬기준 Pinned, 내림차순
        List<Notice> notices = noticeRepository.findAllBySiteIdOrderByPinnedDesc(siteId);
        return notices.stream()
                .map(notice -> NoticeListResponse.builder()
                        .id(notice.getId())
                        .title(notice.getTitle())
                        .category(notice.getCategory())
                        .urgent(notice.isUrgent())
                        .pinned(notice.isPinned())
                        .writer(notice.getWriter().getName())
                        .createdAt(notice.getCreatedAt())
                        .build())
                .toList();
    } 

    // 공지사항 상세 조회, 해당 noticeId를 통해 접근
    public NoticeDetailResponse getNoticeDetail(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));

        return NoticeDetailResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .fileUrl(notice.getFileUrl())
                .category(notice.getCategory())
                .urgent(notice.isUrgent())
                .pinned(notice.isPinned())
                .writer(notice.getWriter().getName())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }

    // 공지사항 작성
    public void createNotice(String title,
                             String content,
                             NoticeCategory category,
                             boolean urgent,
                             boolean pinned,
                             List<MultipartFile> files,
                             Long userId)   {
        User writer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.(createNotice) userId:" + userId));

        Site site = writer.getSite();

        // 공지사항 저장
        Notice notice = noticeRepository.save(
                Notice.builder()
                        .title(title)
                        .content(content)
                        .category(category)
                        .urgent(urgent)
                        .pinned(pinned)
                        .writer(writer)
                        .site(site)
                        .build()
        );

        Notice saved = noticeRepository.save(notice);

        return toResponse(saved);
    }

    // 공지사항 수정
    public NoticeDetailResponse updateNotice(Long noticeId,
                                             String title,
                                             String content,
                                             NoticeCategory category,
                                             boolean urgent,
                                             boolean pinned,
                                             List<MultipartFile> newFiles) {

        // 기존 공지사항 조회
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));

        // 기존 파일 전체 삭제
        fileService.deleteFilesByTarget("NOTICE", noticeId);

        return toResponse(notice);
    }

    // 공지사항 삭제
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
        noticeRepository.delete(notice);
    }

    private NoticeResponse toResponse(Notice notice) {
        return NoticeResponse.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .fileUrl(notice.getFileUrl())
                .category(notice.getCategory())
                .urgent(notice.isUrgent())
                .pinned(notice.isPinned())
                .writerName(notice.getWriter().getName())
                .createdAt(notice.getCreatedAt())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }
}
