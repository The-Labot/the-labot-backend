package com.example.the_labot_backend.payroll.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class PayrollTableResponse {

    private Long payrollId;

    private Long workerId;
    // 근로자 정보
    private String workerName;
    private LocalDate birthDate; // 생년월일 (주민등록번호 대용)

    // 지급 기준
    private String wageType;        // "일급" 또는 "월급" (WageType enum 사용)
    private Long unitPrice;         // 기준단가
    private Double totalManHour;    // 총공수

    // 총 지급액
    private Long totalAmount;

    // 공제 및 실지급액
    private Long totalDeductions;
    private Long netPay;

}
