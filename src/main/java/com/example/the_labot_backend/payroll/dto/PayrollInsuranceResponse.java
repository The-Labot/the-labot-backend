package com.example.the_labot_backend.payroll.dto;

import lombok.Builder;
import lombok.Getter;

// 근로자 리스트 조회
@Getter
@Builder
public class PayrollInsuranceResponse {

    private Long payrollId;

    // 지급/비과세 항목
    private Long totalAmount;       // 지급합계(실제 총 급여)
    private Long mealAllowance;
    private Long monthlySalaryForInsurance;

    // 세금
    private Long incomeTax;
    private Long localIncomeTax;

    // 보험 적용 여부 + 금액
    private Boolean isEmploymentInsuranceApplicable;
    private Long employmentInsuranceAmount;

    private Boolean isNationalPensionApplicable;
    private Long nationalPensionAmount;

    private Boolean isHealthInsuranceApplicable;
    private Long healthInsuranceAmount;

    private Boolean isLongTermCareApplicable;
    private Long longTermCareAmount;

    private Boolean isBasicLivelihoodRecipient;

    // 공제 및 실지급액
    private Long totalDeductions;
    private Long netPay;

    // 퇴직공제
    private Boolean isRetirementDeductionApplicable;
    private Integer retirementDeductionDays;

    // 기타
    private String reasonForLeaving;
}
