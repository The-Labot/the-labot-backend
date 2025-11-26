package com.example.the_labot_backend.sites.dto;

import com.example.the_labot_backend.sites.entity.Site;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SiteDetailResponse {

    private Long siteId;
    private Long headOfficeId;

    private String projectName;
    private String contractType;
    private String siteManagerName;

    private Long contractAmount;
    private String clientName;
    private String primeContractorName;

    private String address;
    private Double latitude;
    private Double longitude;

    private String contractDate;
    private String startDate;
    private String endDate;

    private BankAccountResponse laborCostAccount;

    private String insuranceResponsibility;
    private String employmentInsuranceSiteNum;
    private String primeContractorMgmtNum;
    private boolean isKisconReportTarget;

    private SiteSocialInsResponse socialIns;

    public static SiteDetailResponse from(Site site) {
        return SiteDetailResponse.builder()
                .siteId(site.getId())
                .headOfficeId(site.getHeadOffice().getId())

                .projectName(site.getProjectName())
                .contractType(site.getContractType().name())
                .siteManagerName(site.getSiteManagerName())

                .contractAmount(site.getContractAmount())
                .clientName(site.getClientName())
                .primeContractorName(site.getPrimeContractorName())

                .address(site.getAddress())
                .latitude(site.getLatitude())
                .longitude(site.getLongitude())

                .contractDate(site.getContractDate() != null ? site.getContractDate().toString() : null)
                .startDate(site.getStartDate() != null ? site.getStartDate().toString() : null)
                .endDate(site.getEndDate() != null ? site.getEndDate().toString() : null)

                .laborCostAccount(BankAccountResponse.from(site.getLaborCostAccount()))

                .insuranceResponsibility(site.getInsuranceResponsibility().name())
                .employmentInsuranceSiteNum(site.getEmploymentInsuranceSiteNum())
                .primeContractorMgmtNum(site.getPrimeContractorMgmtNum())
                .isKisconReportTarget(site.isKisconReportTarget())

                .socialIns(site.getSocialIns() != null ? SiteSocialInsResponse.from(site.getSocialIns()) : null)
                .build();
    }
}