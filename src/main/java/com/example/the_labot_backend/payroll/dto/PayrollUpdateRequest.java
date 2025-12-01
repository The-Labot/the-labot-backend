package com.example.the_labot_backend.payroll.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayrollUpdateRequest {

    private Long mealAllowance;              // 식대

    private Long incomeTax;                  // 소득세
    private Long localIncomeTax;             // 지방소득세

    private Boolean isEmploymentInsuranceApplicable;
    private Long employmentInsurance;  // 고용보험

    private Boolean isNationalPensionApplicable;
    private Long nationalPension;      // 국민연금

    private Boolean isHealthInsuranceApplicable;
    private Long healthInsurance;     // 건강보험

    private Boolean isLongTermCareApplicable;
    private Long longTermCare;         // 요양보험

    private Boolean isRetirementDeductionApplicable;
    private String reasonForLeaving;
}

