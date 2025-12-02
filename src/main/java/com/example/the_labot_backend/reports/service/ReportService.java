package com.example.the_labot_backend.reports.service;

import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.files.dto.FileResponse;
import com.example.the_labot_backend.files.service.FileService;
import com.example.the_labot_backend.global.exception.ForbiddenException;
import com.example.the_labot_backend.global.exception.NotFoundException;
import com.example.the_labot_backend.reports.dto.DailyReportListResponse;
import com.example.the_labot_backend.reports.dto.ReportCreateRequest;
import com.example.the_labot_backend.reports.dto.ReportDetailResponse;
import com.example.the_labot_backend.reports.dto.ReportListResponse;
import com.example.the_labot_backend.reports.entity.Report;
import com.example.the_labot_backend.reports.entity.ReportEquipment;
import com.example.the_labot_backend.reports.entity.ReportMaterial;
import com.example.the_labot_backend.reports.repository.ReportEquipmentRepository;
import com.example.the_labot_backend.reports.repository.ReportMaterialRepository;
import com.example.the_labot_backend.reports.repository.ReportRepository;
import com.example.the_labot_backend.sites.entity.Site;
import com.example.the_labot_backend.sites.repository.SiteRepository;
import com.example.the_labot_backend.workers.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserRepository userRepository;
    private final WorkerRepository workerRepository;
    private final FileService fileService;

    private final ReportRepository reportRepository;
    private final ReportEquipmentRepository reportEquipmentRepository;
    private final ReportMaterialRepository reportMaterialRepository;
    private final SiteRepository siteRepository;

    // 작업일보 등록
    @Transactional
    public Long createReport(Long writerId, ReportCreateRequest request) {

        User writer = userRepository.findById(writerId)
                .orElseThrow(() -> new NotFoundException("작성자를 찾을 수 없습니다."));

        Site site = writer.getSite();

        Report report = reportRepository.save(
                Report.builder()
                        .writer(writer)
                        .site(site)
                        .workDate(request.getWorkDate())
                        .workType(request.getWorkType())
                        .todayWork(request.getTodayWork())
                        .tomorrowPlan(request.getTomorrowPlan())
                        .workLocation(request.getWorkLocation())
                        .specialNote(request.getSpecialNote())
                        .workerCount(request.getWorkerCount())
                        .build()
        );

        // 3) 장비 저장
        request.getEquipmentList().forEach(e ->
                reportEquipmentRepository.save(
                        ReportEquipment.builder()
                                .report(report)
                                .equipmentName(e.getEquipmentName())
                                .spec(e.getSpec())
                                .usingTime(e.getUsingTime())
                                .count(e.getCount())
                                .vendorName(e.getVendorName())
                                .build()
                )
        );

        // 4) 자재 저장
        request.getMaterialList().forEach(m ->
                reportMaterialRepository.save(
                        ReportMaterial.builder()
                                .report(report)
                                .materialName(m.getMaterialName())
                                .specAndQuantity(m.getSpecAndQuantity())
                                .importTime(m.getImportTime())
                                .exportDetail(m.getExportDetail())
                                .build()
                )
        );
        
        return report.getId();
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
    public ReportDetailResponse getReportDetail(Long userId, Long reportId) {

        // 1) 작업일보 본체
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("작업일보를 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        // 2) 근로자 목록

        // 3) 장비 목록
        List<ReportEquipment> equipmentList = reportEquipmentRepository.findByReportId(reportId);

        // 4) 자재 목록
        List<ReportMaterial> materialList = reportMaterialRepository.findByReportId(reportId);

        // 파일 조회 (targetType = REPORT)
        List<FileResponse> fileResponses = fileService.getFilesResponseByTarget("REPORT", reportId);

        // 5) DTO로 변환하여 반환
        return ReportDetailResponse.builder()
                .reportId(report.getId())
                .siteId(report.getSite().getId())
                .siteName(report.getSite().getProjectName())
                .writerName(report.getWriter().getName())
                .workDate(report.getWorkDate())
                .workType(report.getWorkType())
                .todayWork(report.getTodayWork())
                .tomorrowPlan(report.getTomorrowPlan())
                .workerCount(report.getWorkerCount())
                .workLocation(report.getWorkLocation())
                .specialNote(report.getSpecialNote())

                // 장비 변환
                .equipmentList(
                        equipmentList.stream().map(e ->
                                ReportDetailResponse.EquipmentInfo.builder()
                                        .equipmentName(e.getEquipmentName())
                                        .spec(e.getSpec())
                                        .usingTime(e.getUsingTime())
                                        .count(e.getCount())
                                        .vendorName(e.getVendorName())
                                        .build()
                        ).toList()
                )

                // 자재 변환
                .materialList(
                        materialList.stream().map(m ->
                                ReportDetailResponse.MaterialInfo.builder()
                                        .materialName(m.getMaterialName())
                                        .specAndQuantity(m.getSpecAndQuantity())
                                        .importTime(m.getImportTime())
                                        .exportDetail(m.getExportDetail())
                                        .build()
                        ).toList()
                )

                // 파일 변환
                .files(
                        fileResponses.stream().map(f ->
                                ReportDetailResponse.FileInfo.builder()
                                        .fileId(f.getId())
                                        .fileUrl(f.getFileUrl())
                                        .originalFileName(f.getOriginalFileName())
                                        .build()
                        ).toList()
                )

                .build();
        }


    // 작업일보 수정
    @Transactional
    public Long updateReport(Long userId, Long reportId, ReportCreateRequest request) {

        // 1) 작업일보 본체
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("작업일보를 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        if(!report.getSite().getId().equals(user.getSite().getId())){
            throw new ForbiddenException("해당 안전교육일지에 접근할 권한이 없습니다.");
        }

        // 2) 기본 정보 수정
        report.setWorkDate(request.getWorkDate());
        report.setWorkType(request.getWorkType());
        report.setTodayWork(request.getTodayWork());
        report.setTomorrowPlan(request.getTomorrowPlan());
        report.setWorkLocation(request.getWorkLocation());
        report.setSpecialNote(request.getSpecialNote());

        // 3) 기존 연관 데이터 삭제
        reportEquipmentRepository.deleteByReportId(reportId);
        reportMaterialRepository.deleteByReportId(reportId);

        // 5) 새 equipment 저장
        if (request.getEquipmentList() != null) {
            request.getEquipmentList().forEach(e ->
                    reportEquipmentRepository.save(
                            ReportEquipment.builder()
                                    .report(report)
                                    .equipmentName(e.getEquipmentName())
                                    .spec(e.getSpec())
                                    .usingTime(e.getUsingTime())
                                    .count(e.getCount())
                                    .vendorName(e.getVendorName())
                                    .build()
                    )
            );
        }

        // 6) 새 material 저장
        if (request.getMaterialList() != null) {
            request.getMaterialList().forEach(m ->
                    reportMaterialRepository.save(
                            ReportMaterial.builder()
                                    .report(report)
                                    .materialName(m.getMaterialName())
                                    .specAndQuantity(m.getSpecAndQuantity())
                                    .importTime(m.getImportTime())
                                    .exportDetail(m.getExportDetail())
                                    .build()
                    )
            );
        }

        // 관련 파일 삭제
        fileService.deleteFilesByTarget("REPORT",reportId);

        return reportId;
    }

    // 작업일보 삭제
    @Transactional
    public void deleteReport(Long userId, Long reportId) {

        // 1) 작업일보 본체
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("작업일보를 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        if(!report.getSite().getId().equals(user.getSite().getId())){
            throw new ForbiddenException("해당 안전교육일지에 접근할 권한이 없습니다.");
        }

        reportEquipmentRepository.deleteByReportId(reportId);
        reportMaterialRepository.deleteByReportId(reportId);
        reportRepository.deleteById(reportId);

        // 관련 파일 삭제
        fileService.deleteFilesByTarget("REPORT",reportId);

    }

    // 작업 상황,장비,자재 조회
    @Transactional(readOnly = true)
    public List<DailyReportListResponse> getReports(Long siteId,
                                                    Long userId,
                                                    int year,
                                                    int month,
                                                    int day
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new RuntimeException("현장을 찾을 수 없습니다."));

        LocalDate date = LocalDate.of(year, month, day);

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        List<Report> reports =
                reportRepository.findDailyReports(siteId, start, end);

        return reports.stream()
                .map(report -> {

                    // 여기서 각 보고서 id로 장비/자재 조회
                    List<ReportEquipment> equipmentList =
                            reportEquipmentRepository.findByReportId(report.getId());

                    List<ReportMaterial> materialList =
                            reportMaterialRepository.findByReportId(report.getId());

                    return DailyReportListResponse.builder()
                            .reportId(report.getId())
                            .siteId(report.getSite().getId())
                            .siteName(report.getSite().getProjectName())
                            .writerName(report.getWriter().getName())
                            .workType(report.getWorkType())
                            .workLocation(report.getWorkLocation())
                            .todayWork(report.getTodayWork())
                            .workerCount(report.getWorkerCount())
                            .specialNote(report.getSpecialNote())
                            .createdAt(report.getCreatedAt())

                            .equipmentList(
                                    equipmentList.stream().map(e ->
                                            DailyReportListResponse.EquipmentInfo.builder()
                                                    .equipmentName(e.getEquipmentName())
                                                    .spec(e.getSpec())
                                                    .usingTime(e.getUsingTime())
                                                    .count(e.getCount())
                                                    .vendorName(e.getVendorName())
                                                    .build()
                                    ).toList()
                            )

                            .materialList(
                                    materialList.stream().map(m ->
                                            DailyReportListResponse.MaterialInfo.builder()
                                                    .materialName(m.getMaterialName())
                                                    .specAndQuantity(m.getSpecAndQuantity())
                                                    .importTime(m.getImportTime())
                                                    .exportDetail(m.getExportDetail())
                                                    .build()
                                    ).toList()
                            )

                            .build();
                })
                .toList();
    }
}
