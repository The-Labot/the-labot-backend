package com.example.the_labot_backend.dashboard.service;

import com.example.the_labot_backend.attendance.entity.Attendance;
import com.example.the_labot_backend.attendance.repository.AttendanceRepository;
import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.global.util.TimeUtils;
import com.example.the_labot_backend.dashboard.dto.DashboardResponse;
import com.example.the_labot_backend.hazards.entity.Hazard;
import com.example.the_labot_backend.hazards.repository.HazardRepository;
import com.example.the_labot_backend.reports.entity.Report;
import com.example.the_labot_backend.reports.repository.ReportRepository;
import java.time.DayOfWeek;

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
                .orElseThrow(() -> new RuntimeException("관리자 정보 없음"));

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
                .orElseThrow(() -> new RuntimeException("관리자 정보 없음"));

        // (2) 조회하려는 현장 확인
        Site targetSite = siteRepository.findById(siteId)
                .orElseThrow(() -> new RuntimeException("현장 정보 없음"));

        // (3) ★ 권한 검증: 내 본사(HeadOffice) 소속 현장이 맞는지?
        if (!admin.getHeadOffice().getId().equals(targetSite.getHeadOffice().getId())) {
            throw new RuntimeException("해당 현장에 대한 접근 권한이 없습니다.");
        }

        // (4) 검증 통과하면 똑같은 로직으로 데이터 생성
        return buildDashboardResponse(siteId);
    }

    // =========================================================
    // ★ 공통 로직 (현장 ID만 주면 데이터를 긁어오는 기계)
    // =========================================================
    private DashboardResponse buildDashboardResponse(Long siteId) {

        // 1. 날짜 기준 설정
        LocalDate todayDate = LocalDate.now();
        DayOfWeek dayOfWeek = todayDate.getDayOfWeek();

        LocalDate targetReportDate; // 작업일보 카운트 기준 날짜

        // [★ 핵심 수정] 토, 일, 월요일은 무조건 '지난 금요일' 데이터를 바라봄
        if (dayOfWeek == DayOfWeek.MONDAY) {
            targetReportDate = todayDate.minusDays(3); // 월 -> 금 (3일 전)
        } else if (dayOfWeek == DayOfWeek.SUNDAY) {
            targetReportDate = todayDate.minusDays(2); // 일 -> 금 (2일 전)
        } else if (dayOfWeek == DayOfWeek.SATURDAY) {
            targetReportDate = todayDate.minusDays(1); // 토 -> 금 (1일 전)
        } else {
            // 화, 수, 목, 금요일은 -> '어제' 데이터를 바라봄
            targetReportDate = todayDate.minusDays(1);
        }

        LocalDateTime startOfDay = todayDate.atStartOfDay();
        LocalDateTime endOfDay = todayDate.atTime(LocalTime.MAX);

        // 2. 통계 카운트
        long hazardCount = hazardRepository.countBySite_IdAndReportedAtBetween(siteId, startOfDay, endOfDay);

        // [수정된 날짜 적용] 금요일(또는 어제) 작업일보 개수
        long reportCount = reportRepository.countBySite_IdAndWorkDate(siteId, targetReportDate);

        long workerCount = workerRepository.countByUser_Site_IdAndStatusNot(siteId, WorkerStatus.RETIRED);

        // 2. 리스트 수집
        List<DashboardResponse.RecentActivity> activityList = new ArrayList<>();

        // (A) 위험요소
        List<Hazard> hazards = hazardRepository.findTop5BySite_IdOrderByReportedAtDesc(siteId);
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

        // (B) 작업일보
        List<Report> reports = reportRepository.findTop5BySite_IdOrderByCreatedAtDesc(siteId);
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

        // (C) 근태
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

        // 3. 정렬 및 자르기
        activityList.sort(Comparator.comparing(DashboardResponse.RecentActivity::getOriginalTime).reversed());
        if (activityList.size() > 10) activityList = activityList.subList(0, 10);

        // 4. 반환
        return DashboardResponse.builder()
                .summary(DashboardResponse.Summary.builder()
                        .todayHazardCount(hazardCount)
                        .ongoingWorkCount(reportCount)
                        .workerCount(workerCount)
                        .build())
                .activities(activityList)
                .build();
    }

    private String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        long seconds = Duration.between(dateTime, LocalDateTime.now()).getSeconds();
        if (seconds < 60) return "방금 전";
        if (seconds < 3600) return (seconds / 60) + "분 전";
        if (seconds < 86400) return (seconds / 3600) + "시간 전";
        return (seconds / 86400) + "일 전";
    }
}
