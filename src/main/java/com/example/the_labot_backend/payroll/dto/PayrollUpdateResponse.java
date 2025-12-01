package com.example.the_labot_backend.payroll.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PayrollUpdateResponse {

    private Long payrollId;

    private Long mealAllowance;

    private Long totalAmount;

    private Long incomeTax;
    private Long localIncomeTax;

    private Long employmentInsuranceAmount;
    private Long nationalPensionAmount;
    private Long healthInsuranceAmount;
    private Long longTermCareAmount;

    private Long totalDeductions;
    private Long netPay;
}
