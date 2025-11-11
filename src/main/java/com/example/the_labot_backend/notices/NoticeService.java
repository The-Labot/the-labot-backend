package com.example.the_labot_backend.notices;

import com.example.the_labot_backend.enums.WorkerStatus;
import com.example.the_labot_backend.files.File;
import com.example.the_labot_backend.files.FileService;
import com.example.the_labot_backend.notices.dto.*;
import com.example.the_labot_backend.notices.entity.Notice;
import com.example.the_labot_backend.notices.entity.NoticeCategory;
import com.example.the_labot_backend.sites.Site;
import com.example.the_labot_backend.sites.SiteRepository;
import com.example.the_labot_backend.users.User;
import com.example.the_labot_backend.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional // ??? 공부하기
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final SiteRepository siteRepository;

    // userId를 통해 현장별 공지사항 목록 조회
    public List<NoticeListResponse> getNoticesByUser(Long userId) {

        // 해당 User 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // user로 siteId 찾기
        Long siteId = user.getSite().getId();

        // 여러개 조회, siteId를 조건으로 조회, 정렬, 정렬기준 Pinned, 내림차순
        List<Notice> notices = noticeRepository.findAllBySite_IdOrderByPinnedDesc(siteId);
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

    // noticeId를 통해 공지사항 상세 조회
    public NoticeDetailResponse getNoticeDetail(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));

        List<File> files = fileService.getFilesByTarget("NOTICE", noticeId);

        return new NoticeDetailResponse(notice, files);
    }

    // 공지사항 작성
    public void createNotice(String title, String content, NoticeCategory category,
                                       boolean urgent, boolean pinned, List<MultipartFile> files , Long writerId) {

        User writer = userRepository.findById(writerId)
                .orElseThrow(() -> new RuntimeException("작성자 정보를 찾을 수 없습니다."));


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

        // 파일 업로드 (로컬 or S3)
        fileService.saveFiles(files, "NOTICE", notice.getId());
    }

    // 공지사항 수정
    public NoticeDetailResponse updateNotice(Long noticeId,
                                             String title,
                                             String content,
                                             NoticeCategory category,
                                             boolean urgent,
                                             boolean pinned,
                                             List<Long> deleteFileIds,
                                             List<MultipartFile> newFiles) {

        // 기존 공지사항 조회
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));

        // 내용 수정
        notice.update(title, content, category, urgent, pinned);

        // 삭제할 파일이 있으면 삭제
        if (deleteFileIds != null && !deleteFileIds.isEmpty()) {
            for (Long fileId : deleteFileIds) {
                fileService.deleteFile(fileId);
            }
        }

        // 새 파일 업로드
        if (newFiles != null && !newFiles.isEmpty()) {
            fileService.saveFiles(newFiles, "NOTICE", notice.getId());
        }

        // 최신 파일 목록 조회
        List<File> files = fileService.getFilesByTarget("NOTICE", noticeId);

        // 최신 공지사항 정보 + 파일 함께 반환
        return new NoticeDetailResponse(notice, files);
    }

    // 공지사항 삭제
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));
        noticeRepository.delete(notice);
    }
}
