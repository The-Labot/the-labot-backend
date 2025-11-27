package com.example.the_labot_backend.reports.service;

import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.files.dto.FileResponse;
import com.example.the_labot_backend.files.service.FileService;
import com.example.the_labot_backend.global.exception.ForbiddenException;
import com.example.the_labot_backend.global.exception.NotFoundException;
import com.example.the_labot_backend.reports.dto.ReportCreateRequest;
import com.example.the_labot_backend.reports.dto.ReportDetailResponse;
import com.example.the_labot_backend.reports.dto.ReportListResponse;
import com.example.the_labot_backend.reports.entity.Report;
import com.example.the_labot_backend.reports.entity.ReportEquipment;
import com.example.the_labot_backend.reports.entity.ReportMaterial;
import com.example.the_labot_backend.reports.entity.ReportWorker;
import com.example.the_labot_backend.reports.repository.ReportEquipmentRepository;
import com.example.the_labot_backend.reports.repository.ReportMaterialRepository;
import com.example.the_labot_backend.reports.repository.ReportRepository;
import com.example.the_labot_backend.reports.repository.ReportWorkerRepository;
import com.example.the_labot_backend.sites.entity.Site;
import com.example.the_labot_backend.workers.entity.Worker;
import com.example.the_labot_backend.workers.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final UserRepository userRepository;
    private final WorkerRepository workerRepository;
    private final FileService fileService;

    private final ReportRepository reportRepository;
    private final ReportWorkerRepository reportWorkerRepository;
    private final ReportEquipmentRepository reportEquipmentRepository;
    private final ReportMaterialRepository reportMaterialRepository;

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
                        .build()
        );

        // 2) 작업 참여 근로자 저장
        if (request.getWorkers() != null) {
            for (Long workerId : request.getWorkers()) {

                Worker worker = workerRepository.findById(workerId)
                        .orElseThrow(() -> new NotFoundException("근로자를 찾을 수 없습니다: " + workerId));

                ReportWorker reportWorker =
                        ReportWorker.builder()
                                .report(report)
                                .worker(worker)
                                .build();

                reportWorkerRepository.save(reportWorker);
            }
        }

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

        if(!report.getSite().getId().equals(user.getSite().getId())){
            throw new ForbiddenException("해당 안전교육일지에 접근할 권한이 없습니다.");
        }

        // 2) 근로자 목록
        List<ReportWorker> workers = reportWorkerRepository.findByReportId(reportId);

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
                .workLocation(report.getWorkLocation())
                .specialNote(report.getSpecialNote())

                // 근로자 변환
                .workers(
                        workers.stream().map(w -> {
                            // workerId로 Worker 엔티티 조회
                            Worker worker = workerRepository.findById(w.getWorker().getId())
                                    .orElseThrow(() -> new NotFoundException("근로자를 찾을 수 없습니다. ID=" + w.getWorker().getId()));

                            return ReportDetailResponse.WorkerInfo.builder()
                                    .workerId(worker.getId())
                                    .workerName(worker.getUser().getName())   // 이름 매핑
                                    .birthDate(worker.getBirthDate() != null ? worker.getBirthDate() : null)
                                    .position(worker.getPosition() != null ? worker.getPosition() : null)
                                    .build();
                        }).toList()
                )

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
        reportWorkerRepository.deleteByReportId(reportId);
        reportEquipmentRepository.deleteByReportId(reportId);
        reportMaterialRepository.deleteByReportId(reportId);

        // 4) 새 workers 저장
        if (request.getWorkers() != null) {
            for (Long workerId : request.getWorkers()) {

                Worker worker = workerRepository.findById(workerId)
                        .orElseThrow(() -> new NotFoundException("근로자를 찾을 수 없습니다: " + workerId));

                ReportWorker reportWorker =
                        ReportWorker.builder()
                                .report(report)
                                .worker(worker)
                                .build();

                reportWorkerRepository.save(reportWorker);
            }
        }

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

        reportWorkerRepository.deleteByReportId(reportId);
        reportEquipmentRepository.deleteByReportId(reportId);
        reportMaterialRepository.deleteByReportId(reportId);
        reportRepository.deleteById(reportId);

        // 관련 파일 삭제
        fileService.deleteFilesByTarget("REPORT",reportId);

    }
}
