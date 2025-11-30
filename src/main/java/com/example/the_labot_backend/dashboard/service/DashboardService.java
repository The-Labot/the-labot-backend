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
    private final HazardRepository hazardRepository;
    private final ReportRepository reportRepository;
    private final AttendanceRepository attendanceRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long userId) {

        // 1. 관리자 정보로 현장 ID 찾기
        User manager = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("관리자 정보가 없습니다."));
        Long siteId = manager.getSite().getId();

        // 2. 오늘 날짜 범위 설정
        LocalDate todayDate = LocalDate.now();
        LocalDateTime startOfDay = todayDate.atStartOfDay();
        LocalDateTime endOfDay = todayDate.atTime(LocalTime.MAX);

        // ==========================================
        //  [1] 상단 통계 카드 데이터 조회
        // ==========================================
        long hazardCount = hazardRepository.countBySite_IdAndReportedAtBetween(siteId, startOfDay, endOfDay);
        long reportCount = reportRepository.countBySite_IdAndWorkDate(siteId, todayDate);
        long workerCount = attendanceRepository.countByWorker_User_Site_IdAndDate(siteId, todayDate);

        // ==========================================
        //  [2] 최근 활동 리스트 데이터 수집 (Merge)
        // ==========================================
        List<DashboardResponse.RecentActivity> activityList = new ArrayList<>();

        // (A) 위험요소 5개 가져와서 리스트에 추가
        List<Hazard> hazards = hazardRepository.findTop5BySite_IdOrderByReportedAtDesc(siteId);
        for (Hazard h : hazards) {
            activityList.add(DashboardResponse.RecentActivity.builder()
                    .id(h.getId())
                    .type("HAZARD")
                    .title("위험요소 신고")
                    .description(h.getHazardType() + " · " + h.getLocation())
                    .timeAgo(formatTimeAgo(h.getReportedAt()))
                    .status(h.isUrgent() ? "긴급" : "일반")
                    .originalTime(h.getReportedAt()) // 정렬 기준 시간
                    .build());
        }

        // (B) 작업일보 5개 가져와서 리스트에 추가
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

        // (C) 출근 기록 5개 가져와서 리스트에 추가
        List<Attendance> attendances = attendanceRepository.findTop5ByWorker_User_Site_IdAndDateOrderByClockInTimeDesc(siteId, todayDate);
        for (Attendance a : attendances) {
            // 출근시간(LocalTime)을 날짜와 합쳐서 LocalDateTime으로 변환
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

        // ==========================================
        //  [3] 시간순 정렬 및 자르기
        // ==========================================
        // 최신순(내림차순) 정렬
        activityList.sort(Comparator.comparing(DashboardResponse.RecentActivity::getOriginalTime).reversed());

        // 너무 많으면 상위 10개만 남김
        if (activityList.size() > 10) {
            activityList = activityList.subList(0, 10);
        }

        // ==========================================
        //  [4] 최종 응답 생성
        // ==========================================
        return DashboardResponse.builder()
                .summary(DashboardResponse.Summary.builder()
                        .todayHazardCount(hazardCount)
                        .ongoingWorkCount(reportCount)
                        .workerCount(workerCount)
                        .build())
                .activities(activityList)
                .build();
    }

    // [유틸 메서드] 시간을 "5분 전" 같은 문자열로 변환
    private String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        long seconds = Duration.between(dateTime, LocalDateTime.now()).getSeconds();

        if (seconds < 60) return "방금 전";
        if (seconds < 3600) return (seconds / 60) + "분 전";
        if (seconds < 86400) return (seconds / 3600) + "시간 전";
        return (seconds / 86400) + "일 전";
    }
}
