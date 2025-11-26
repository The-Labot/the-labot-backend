package com.example.the_labot_backend.payroll.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class PayrollTableResponse {

    private Long payrollId;

    // 근로자 정보
    private String workerName;
    private LocalDate birthDate; // 생년월일 (주민등록번호 대용)

    // 지급 기준
    private String wageType;        // "일급" 또는 "월급" (WageType enum 사용)
    private Long unitPrice;         // 기준단가
    private Double totalManHour;    // 총공수

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
