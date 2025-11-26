package com.example.the_labot_backend.notices.service;

import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.files.entity.File;
import com.example.the_labot_backend.files.service.FileService;
import com.example.the_labot_backend.global.exception.BadRequestException;
import com.example.the_labot_backend.global.exception.ForbiddenException;
import com.example.the_labot_backend.global.exception.NotFoundException;
import com.example.the_labot_backend.notices.dto.NoticeCreateForm;
import com.example.the_labot_backend.notices.dto.NoticeDetailResponse;
import com.example.the_labot_backend.notices.dto.NoticeListResponse;
import com.example.the_labot_backend.notices.entity.Notice;
import com.example.the_labot_backend.notices.repository.NoticeRepository;
import com.example.the_labot_backend.sites.entity.Site;
import com.example.the_labot_backend.sites.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final SiteRepository siteRepository;

    // 공지사항 작성
    @Transactional
    public void createNotice(Long userId, NoticeCreateForm form, List<MultipartFile> files){

        if (form.getTitle() == null || form.getTitle().trim().isEmpty()) {
            throw new BadRequestException("공지사항 제목은 필수 입력 항목입니다.");
        }
        if (form.getContent() == null || form.getContent().trim().isEmpty()) {
            throw new BadRequestException("공지사항 내용은 필수 입력 항목입니다.");
        }
        if (form.getCategory() == null) {
            throw new BadRequestException("공지사항 카테고리는 필수 입력 항목입니다.");
        }
        if (userId == null) {
            throw new BadRequestException("작성자 정보가 누락되었습니다.");
        }

        User user = getCurrentUser();
        Site site = getUserSite(user);

        // 공지사항 저장
        Notice notice = noticeRepository.save(
                Notice.builder()
                        .title(form.getTitle())
                        .content(form.getContent())
                        .category(form.getCategory())
                        .urgent(form.isUrgent())
                        .pinned(form.isPinned())
                        .writer(user)
                        .site(site)
                        .build()
        );

        // 파일 업로드 (로컬 or S3)
        fileService.saveFiles(files, "NOTICE", notice.getId());
    }

    // userId를 통해 현장별 공지사항 목록 조회
    @Transactional(readOnly = true)
    public List<NoticeListResponse> getNoticesByUser(Long userId, String title) {

        User user = getCurrentUser();
        Site site = getUserSite(user);

        List<Notice> notices;

        // 여러개 조회, siteId를 조건으로 조회, 정렬, 정렬기준 Pinned&Urgent, 내림차순, (title 필터링)
        if (title == null || title.trim().isEmpty()) {
            notices = noticeRepository
                    .findAllBySite_IdOrderByPinnedDescUrgentDescCreatedAtDesc(site.getId());
        } else {
            notices = noticeRepository
                    .findAllBySite_IdAndTitleContainingIgnoreCaseOrderByPinnedDescUrgentDescCreatedAtDesc(
                            site.getId(),
                            title.trim()
                    );
        }

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
    @Transactional(readOnly = true)
    public NoticeDetailResponse getNoticeDetail(Long noticeId) {

        User user = getCurrentUser();
        Site site = getUserSite(user);

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NotFoundException("공지사항을 찾을 수 없습니다. noticeId:" + noticeId));

        if (!site.getId().equals(notice.getSite().getId())) {
            throw new ForbiddenException("해당 공지사항에 접근 권한이 없습니다.");
        }

        List<File> files = fileService.getFilesByTarget("NOTICE", noticeId);

        return new NoticeDetailResponse(notice, files);
    }



    // 공지사항 수정
    @Transactional
    public NoticeDetailResponse updateNotice(Long noticeId,
                                             NoticeCreateForm form,
                                             List<MultipartFile> newFiles) {

        if (form.getTitle() == null || form.getTitle().trim().isEmpty()) {
            throw new BadRequestException("공지사항 제목은 필수 입력 항목입니다.");
        }
        if (form.getContent() == null || form.getContent().trim().isEmpty()) {
            throw new BadRequestException("공지사항 내용은 필수 입력 항목입니다.");
        }
        if (form.getCategory() == null) {
            throw new BadRequestException("공지사항 카테고리는 필수 입력 항목입니다.");
        }

        User user = getCurrentUser();
        Site site = getUserSite(user);

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NotFoundException("공지사항을 찾을 수 없습니다.noticeId:" + noticeId));

        if (!site.getId().equals(notice.getSite().getId())) {
            throw new ForbiddenException("해당 공지사항에 접근 권한이 없습니다.");
        }
        
        // 내용 수정
        notice.setTitle(form.getTitle());
        notice.setContent(form.getContent());
        notice.setCategory(form.getCategory());
        notice.setUrgent(form.isUrgent());
        notice.setPinned(form.isPinned());

        // 기존 파일 전체 삭제
        fileService.deleteFilesByTarget("NOTICE", noticeId);

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
    @Transactional
    public void deleteNotice(Long noticeId) {

        User user = getCurrentUser();
        Site site = getUserSite(user);

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NotFoundException("공지사항을 찾을 수 없습니다. noticeId:" + noticeId));

        if (!site.getId().equals(notice.getSite().getId())) {
            throw new ForbiddenException("해당 공지사항에 접근 권한이 없습니다.");
        }

        // 공지사항에 연결된 파일 모두 삭제
        fileService.deleteFilesByTarget("NOTICE", noticeId);

        // 공지사항 삭제
        noticeRepository.delete(notice);
    }

    // 현재 유저 찾기
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. userId:" + userId));
    }
    
    // 현재 유저의 사이트 찾기
    private Site getUserSite(User user) {
        Site site = user.getSite();
        if (site == null) {
            throw new NotFoundException("해당 사용자는 현재 소속된 현장이 없습니다.");
        }
        return site;
    }
}
