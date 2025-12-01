package com.example.the_labot_backend.notices.worker.service;

import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.files.dto.FileResponse;
import com.example.the_labot_backend.files.entity.File;
import com.example.the_labot_backend.files.service.FileService;
import com.example.the_labot_backend.notices.repository.NoticeRepository;
import com.example.the_labot_backend.notices.entity.Notice;
import com.example.the_labot_backend.notices.entity.NoticeCategory;
import com.example.the_labot_backend.notices.worker.dto.WorkerNoticeDetailDto;
import com.example.the_labot_backend.notices.worker.dto.WorkerNoticeListDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkerNoticeService {
    private final NoticeRepository noticeRepository;
    private final FileService fileService;
    /**
     * 목록 조회 (필터링 및 날짜)
     */
    public List<WorkerNoticeListDto> getNoticesForWorker(User user, String categoryStr, String yearMonthStr) {

        // ▼▼▼ [★ 1. 수정: NPE 방지 코드 추가] ▼▼▼
        // 유저에게 배정된 현장이 없으면 에러 내지 말고 빈 목록 반환
        if (user.getSite() == null) {
            return Collections.emptyList();
        }
        // ▲▲▲ [★ 1. 여기까지 추가] ▲▲▲

        Long siteId = user.getSite().getId();

        // 2. [날짜 계산]
        LocalDateTime startDate;
        LocalDateTime endDate;

        if (yearMonthStr != null && !yearMonthStr.isBlank()) {
            try {
                YearMonth ym = YearMonth.parse(yearMonthStr);
                startDate = ym.atDay(1).atStartOfDay();
                endDate = ym.atEndOfMonth().atTime(LocalTime.MAX);
            } catch (Exception e) {
                // 날짜 형식이 이상하면 기본값으로
                startDate = LocalDate.now().minusYears(1).atStartOfDay();
                endDate = LocalDate.now().atTime(LocalTime.MAX);
            }
        } else {
            // ▼▼▼ [★ 2. 수정: 시간 범위 명확화] ▼▼▼
            // "오늘 기준 1년 전 0시 0분" ~ "오늘 밤 23시 59분"
            // (LocalDateTime.now()를 쓰면 초 단위 차이로 오늘 글이 안 보일 수 있음)
            startDate = LocalDate.now().minusYears(1).atStartOfDay();
            endDate = LocalDate.now().atTime(LocalTime.MAX);
            // ▲▲▲ [★ 2. 여기까지 수정] ▲▲▲
        }


        // 3. [카테고리 변환]
        NoticeCategory category = null;
        if (!"ALL".equalsIgnoreCase(categoryStr)) {
            try {
                category = NoticeCategory.valueOf(categoryStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                category = null;
            }
        }

        // 4. [DB 조회]
        List<Notice> notices = noticeRepository.findNoticesForWorker(siteId, category, startDate, endDate);

        return notices.stream()
                .map(WorkerNoticeListDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 상세 조회
     */
    public WorkerNoticeDetailDto getNoticeDetailForWorker(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("공지사항을 찾을 수 없습니다. ID: " + noticeId));

        // 2. [★ 수정됨] 파일 DTO 목록 바로 조회 (엔티티 조회 X)
        // FileService의 getFilesResponseByTarget 메서드를 사용하여 바로 DTO 리스트를 받습니다.
        List<FileResponse> fileResponses = fileService.getFilesResponseByTarget("NOTICE", noticeId);

        // 3. 조립해서 반환
        return WorkerNoticeDetailDto.of(notice, fileResponses);
    }
}
