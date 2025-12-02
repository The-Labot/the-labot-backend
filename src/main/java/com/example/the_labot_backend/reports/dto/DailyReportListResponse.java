package com.example.the_labot_backend.reports.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class DailyReportListResponse {

    private Long reportId;
    private Long siteId;
    private String siteName;
    private String writerName;

    // 작업 요약
    private String workType;
    private String workLocation;
    private String todayWork;
    private int workerCount;
    private String specialNote;
    private LocalDateTime createdAt;
    
    // 장비 & 자재
    private List<EquipmentInfo> equipmentList;
    private List<MaterialInfo> materialList;

    @Getter
    @Builder
    public static class EquipmentInfo {
        private String equipmentName;
        private String spec;
        private String usingTime;
        private int count;
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
}

