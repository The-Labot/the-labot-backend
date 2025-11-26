package com.example.the_labot_backend.payroll.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayrollInsuranceUpdateRequest {
    private Boolean employmentInsurance;    // 고용보험 여부
    private Boolean nationalPension;        // 국민연금 여부
    private Boolean healthInsurance;        // 건강보험 여부
    private Boolean longTermCare;           // 요양보험 여부
}