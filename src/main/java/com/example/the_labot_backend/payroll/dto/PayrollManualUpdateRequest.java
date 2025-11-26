package com.example.the_labot_backend.payroll.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayrollManualUpdateRequest {

    private Long mealAllowance;              // 식대

    private Long incomeTaxManual;                  // 소득세
    private Long localIncomeTaxManual;             // 지방소득세

    private Long employmentInsuranceManual;  // 고용보험
    private Long nationalPensionManual;      // 국민연금
    private Long healthInsuranceManual;     // 건강보험
    private Long longTermCareManual;         // 요양보험
}

