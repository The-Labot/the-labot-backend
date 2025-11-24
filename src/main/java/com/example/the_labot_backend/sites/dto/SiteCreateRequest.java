package com.example.the_labot_backend.sites.dto;

import com.example.the_labot_backend.sites.entity.ContractType;
import com.example.the_labot_backend.sites.entity.InsuranceResponsibility;
import com.example.the_labot_backend.sites.entity.embeddable.PayrollBankAccount;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class SiteCreateRequest {

    // 기본 정보
    private String projectName;
    private ContractType contractType; // PRIME, SUB
    private String siteManagerName;
    private Long contractAmount;
    private String clientName;
    private String primeContractorName;

    // 위치
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

    // 설정 및 책임
    private InsuranceResponsibility insuranceResponsibility;
    private String primeContractorMgmtNum;
    private boolean isKisconReportTarget;

    // 포함된 사회보험 정보 DTO
    private SiteSocialInsDto socialIns;

    public PayrollBankAccount toBankAccount() {
        return PayrollBankAccount.builder()
                .bankName(laborCostBankName)
                .accountNumber(laborCostAccountNumber)
                .accountHolder(laborCostAccountHolder)
                .informPhoneNumber(informPhoneNumber)
                .build();
    }
}