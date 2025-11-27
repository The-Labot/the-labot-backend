package com.example.the_labot_backend.reports.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class ReportDetailResponse {
    private Long reportId;

    // 기본 정보
    private Long siteId;
    private String siteName;
    private String writerName;
    private LocalDate workDate;
    private String workType;
    private String todayWork;
    private String tomorrowPlan;
    private String workLocation;
    private String specialNote;

    // 근로자 목록
    private List<WorkerInfo> workers;

    // 장비 목록
    private List<EquipmentInfo> equipmentList;

    // 자재 목록
    private List<MaterialInfo> materialList;

    // 사진 목록 (File 엔티티 활용)
    private List<FileInfo> files;

    // DTO 내부 정적 클래스들
    @Getter
    @Builder
    public static class WorkerInfo {
        private Long workerId;
        private String workerName;
        private LocalDate birthDate; // 생년월일
        private String position; // 직종 (배관공, 전기공 등)
    }

    @Getter
    @Builder
    public static class EquipmentInfo {
        private String equipmentName;
        private String spec;
        private String usingTime;
        private Integer count;
        private String vendorName;
    }

    @Getter
    @Builder
    public static class MaterialInfo {
        private String materialName;
        private String specAndQuantity;
        private String importTime;
        private String exportDetail;
    }

    @Getter
    @Builder
    public static class FileInfo {
        private Long fileId;
        private String fileUrl;
        private String originalFileName;
    }
}
