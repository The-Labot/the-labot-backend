package com.example.the_labot_backend.sites.dto;

import com.example.the_labot_backend.sites.entity.ContractType;
import com.example.the_labot_backend.sites.entity.InsuranceResponsibility;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class SiteUpdateRequest {

    // 기본 정보
    private String projectName;
    private ContractType contractType;
    private String siteManagerName;
    private Long contractAmount;
    private String clientName;
    private String primeContractorName;

    // 위치 정보
    private String address;
    private Double latitude;
    private Double longitude;

    // 기간
    private LocalDate contractDate;
    private LocalDate startDate;
    private LocalDate endDate;

    // 노무비 계좌
    private String laborCostBankName;
    private String laborCostAccountNumber;
    private String laborCostAccountHolder;
    private String informPhoneNumber;

    // 보험/행정
    private InsuranceResponsibility insuranceResponsibility;
    private String employmentInsuranceSiteNum;
    private String primeContractorMgmtNum;
    private Boolean kisconReportTarget;

    // 사회보험 정보 수정 (부분 수정 가능)
    private SiteSocialInsDto socialIns;
}

