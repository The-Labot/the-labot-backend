package com.example.the_labot_backend.dashboard.service;

import com.example.the_labot_backend.attendance.entity.Attendance;
import com.example.the_labot_backend.attendance.repository.AttendanceRepository;
import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.dashboard.dto.DashboardResponse;
import com.example.the_labot_backend.global.exception.ForbiddenException;
import com.example.the_labot_backend.global.exception.NotFoundException;
import com.example.the_labot_backend.hazards.entity.Hazard;
import com.example.the_labot_backend.hazards.repository.HazardRepository;
import com.example.the_labot_backend.reports.entity.Report;
import com.example.the_labot_backend.reports.repository.ReportRepository;
import com.example.the_labot_backend.sites.entity.Site;
import com.example.the_labot_backend.sites.repository.SiteRepository;
import com.example.the_labot_backend.workers.entity.WorkerStatus;
import com.example.the_labot_backend.workers.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Service
@RequiredArgsConstructor
public class DashboardService {
    private final UserRepository userRepository;
    private final WorkerRepository workerRepository;
    private final SiteRepository siteRepository; // ★ 추가됨
    private final HazardRepository hazardRepository;
    private final ReportRepository reportRepository;
    private final AttendanceRepository attendanceRepository;

    // =========================================================
    // 1. 현장 소장용 (내 현장 보기)
    // =========================================================
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardForManager(Long userId) {
        User manager = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("관리자 정보 없음"));

        // 내 소속 현장 ID 사용
        return buildDashboardResponse(manager.getSite().getId());
    }

    // =========================================================
    // 2. [신규] 본사 관리자용 (특정 현장 선택해서 보기)
    // =========================================================
    @Transactional(readOnly = true)
    public DashboardResponse getDashboardForHeadOffice(Long adminId, Long siteId) {
        // (1) 본사 관리자 확인
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("관리자 정보 없음"));

        // (2) 조회하려는 현장 확인
        Site targetSite = siteRepository.findById(siteId)
                .orElseThrow(() -> new NotFoundException("현장 정보 없음"));

        // (3) ★ 권한 검증: 내 본사(HeadOffice) 소속 현장이 맞는지?
        if (!admin.getHeadOffice().getId().equals(targetSite.getHeadOffice().getId())) {
            throw new ForbiddenException("해당 현장에 대한 접근 권한이 없습니다.");
        }

        // (4) 검증 통과하면 똑같은 로직으로 데이터 생성
        return buildDashboardResponse(siteId);
    }

    // =========================================================
    // ★ 공통 로직 (현장 ID만 주면 데이터를 긁어오는 기계)
    // =========================================================
    private DashboardResponse buildDashboardResponse(Long siteId) {

        // 1. 시간 기준값 설정
        LocalDateTime now = LocalDateTime.now();
        LocalDate todayDate = LocalDate.now();

        LocalDateTime startOfDay = todayDate.atStartOfDay();
        LocalDateTime endOfDay = todayDate.atTime(LocalTime.MAX);
        LocalDateTime twentyFourHoursAgo = now.minusHours(24);


        // (1) 위험요소: 오늘 하루 발생 건수
        long hazardCount = hazardRepository.countBySite_IdAndReportedAtBetween(siteId, startOfDay, endOfDay);

        // (2) 작업일보: 최근 24시간 이내 작성 건수
        long reportCount = reportRepository.countBySite_IdAndCreatedAtAfter(siteId, twentyFourHoursAgo);

        // (3) [★ 수정됨] 근로자 수: 현재 'ACTIVE' 상태인 사람만 카운트
        long workerCount = workerRepository.countByUser_Site_IdAndStatus(siteId, WorkerStatus.ACTIVE);

        List<DashboardResponse.RecentActivity> activityList = new ArrayList<>();

        // (A) 위험요소 리스트: [오늘] 발생한 것
        List<Hazard> hazards = hazardRepository.findTop5BySite_IdAndReportedAtBetweenOrderByReportedAtDesc(siteId, startOfDay, endOfDay);
        for (Hazard h : hazards) {
            activityList.add(DashboardResponse.RecentActivity.builder()
                    .id(h.getId())
                    .type("HAZARD")
                    .title("위험요소 신고")
                    .description(h.getHazardType() + " · " + h.getLocation())
                    .timeAgo(formatTimeAgo(h.getReportedAt()))
                    .status(h.isUrgent() ? "긴급" : "일반")
                    .originalTime(h.getReportedAt())
                    .build());
        }

        // (B) 작업일보 리스트: [최근 24시간] 이내 작성된 것
        List<Report> reports = reportRepository.findTop5BySite_IdAndCreatedAtAfterOrderByCreatedAtDesc(siteId, twentyFourHoursAgo);
        for (Report r : reports) {
            activityList.add(DashboardResponse.RecentActivity.builder()
                    .id(r.getId())
                    .type("REPORT")
                    .title("작업 일보 제출")
                    .description(r.getWorkType() + " · " + r.getWriter().getName())
                    .timeAgo(formatTimeAgo(r.getCreatedAt()))
                    .status("제출완료")
                    .originalTime(r.getCreatedAt())
                    .build());
        }

        // (C) 출근 리스트: [오늘] 출근한 기록
        // (ACTIVE 상태가 아니더라도 오늘 출근 이력은 보여주는 것이 좋으므로 Attendance 테이블 사용)
        List<Attendance> attendances = attendanceRepository.findTop5ByWorker_User_Site_IdAndDateOrderByClockInTimeDesc(siteId, todayDate);
        for (Attendance a : attendances) {
            LocalDateTime clockInDateTime = LocalDateTime.of(a.getDate(), a.getClockInTime());
            activityList.add(DashboardResponse.RecentActivity.builder()
                    .id(a.getId())
                    .type("ATTENDANCE")
                    .title("근로자 출근")
                    .description(a.getWorker().getUser().getName() + " · 출근 완료")
                    .timeAgo(formatTimeAgo(clockInDateTime))
                    .status(a.getStatus().name())
                    .originalTime(clockInDateTime)
                    .build());
        }

        // 4. 통합 정렬 및 자르기 (최신순 10개)
        activityList.sort(Comparator.comparing(DashboardResponse.RecentActivity::getOriginalTime).reversed());
        if (activityList.size() > 10) activityList = activityList.subList(0, 10);

        return DashboardResponse.builder()
                .summary(DashboardResponse.Summary.builder()
                        .todayHazardCount(hazardCount)
                        .ongoingWorkCount(reportCount)
                        .workerCount(workerCount) // ACTIVE 상태인 사람 수
                        .build())
                .activities(activityList)
                .build();
    }

    // 시간 포맷 헬퍼 메서드
    private String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        long seconds = Duration.between(dateTime, LocalDateTime.now()).getSeconds();
        if (seconds < 60) return "방금 전";
        if (seconds < 3600) return (seconds / 60) + "분 전";
        if (seconds < 86400) return (seconds / 3600) + "시간 전";
        return (seconds / 86400) + "일 전";
    }
}
