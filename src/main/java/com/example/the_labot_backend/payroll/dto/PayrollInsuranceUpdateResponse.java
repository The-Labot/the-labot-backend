package com.example.the_labot_backend.payroll.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PayrollInsuranceUpdateResponse {
    private Long payrollId;

    private Boolean employmentInsurance;
    private Boolean nationalPension;
    private Boolean healthInsurance;
    private Boolean longTermCare;

    private Long incomeTax;
    private Long localIncomeTax;

    private Long employmentInsuranceAmount;
    private Long nationalPensionAmount;
    private Long healthInsuranceAmount;
    private Long longTermCareAmount;

    private Long totalDeductions;
    private Long netPay;
}
