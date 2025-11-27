package com.example.the_labot_backend.reports.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class ReportCreateRequest {

    private String workType;           // 공종명
    private LocalDate workDate;        // 작업 날짜
    private String todayWork;          // 금일 작업
    private String tomorrowPlan;       // 명일 작업 계획
    private String workLocation;       // 작업 위치
    private String specialNote;        // 특이사항

    private List<Long> workers;           // 작업자 목록
    private List<EquipmentRequest> equipmentList;  // 장비 목록
    private List<MaterialRequest> materialList;    // 자재 목록

    @Getter
    public static class EquipmentRequest {
        private String equipmentName;
        private String spec;
        private String usingTime;
        private Integer count;
        private String vendorName;
    }

    @Getter
    public static class MaterialRequest {
        private String materialName;
        private String specAndQuantity;
        private String importTime;
        private String exportDetail;
    }
}
