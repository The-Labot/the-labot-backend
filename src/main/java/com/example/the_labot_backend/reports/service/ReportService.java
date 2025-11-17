package com.example.the_labot_backend.reports.service;

import com.example.the_labot_backend.authUser.entity.User;
import com.example.the_labot_backend.authUser.repository.UserRepository;
import com.example.the_labot_backend.reports.dto.ReportListResponse;
import com.example.the_labot_backend.reports.dto.ReportRequest;
import com.example.the_labot_backend.reports.dto.ReportResponse;
import com.example.the_labot_backend.reports.entity.Report;
import com.example.the_labot_backend.reports.repository.ReportRepository;
import com.example.the_labot_backend.sites.entity.Site;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    // 작업일보 등록
    public ReportResponse createReport(Long userId, ReportRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.(getReportsByUser) userId:" + userId));

        Site site = user.getSite();

        Report report = Report.builder()
                .writer(user)
                .site(site)
                .workType(request.getWorkType())
                .todayWork(request.getTodayWork())
                .tomorrowPlan(request.getTomorrowPlan())
                .manpowerCount(request.getManpowerCount())
                .materialStatus(request.getMaterialStatus())
                .equipmentStatus(request.getEquipmentStatus())
                .specialNote(request.getSpecialNote())
                .build();

        return ReportResponse.from(reportRepository.save(report));
    }

    // userId를 통해 현장별 작업일보 조회
    @Transactional(readOnly = true)
    public List<ReportListResponse> getReportsByUser(Long userId) {
        
        // 해당 User 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.(getReportsByUser) userId:" + userId));

        // user로 siteId 찾기
        Long siteId = user.getSite().getId();

        return reportRepository.findAllBySite_IdOrderByCreatedAtDesc(siteId) // report 조회, 리스트로 반환
                .stream() // report 리스트를 하나씩 처리함.
                .map(ReportListResponse::from) // Report 엔티티를 ReportListResponse 엔티티로 변환
                .collect(Collectors.toList()); // 리스트로 재 생성 후 반환
    }

    // reportId를 통해 작업일보 상세 조회
    @Transactional(readOnly = true)
    public ReportResponse getReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("작업일보를 찾을 수 없습니다.(getReport) reportId:" + reportId));
        return ReportResponse.from(report);
    }

    // 작업일보 수정
    public ReportResponse updateReport(Long reportId, ReportRequest request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("작업일보를 찾을 수 없습니다.(getReport) reportId:" + reportId));

        report.setWorkType(request.getWorkType());
        report.setTodayWork(request.getTodayWork());
        report.setTomorrowPlan(request.getTomorrowPlan());
        report.setManpowerCount(request.getManpowerCount());
        report.setMaterialStatus(request.getMaterialStatus());
        report.setEquipmentStatus(request.getEquipmentStatus());
        report.setSpecialNote(request.getSpecialNote());

        return ReportResponse.from(reportRepository.save(report));
    }

    // 작업일보 삭제
    public void deleteReport(Long reportId) {
        if (!reportRepository.existsById(reportId)) {
            throw new RuntimeException("작업일보를 찾을 수 없습니다.(getReport) reportId:" + reportId);
        }
        reportRepository.deleteById(reportId);
    }
}
