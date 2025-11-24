package com.example.the_labot_backend.sites.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class SiteSocialInsDto {

    // ===========================================
    // 1. 국민연금 (National Pension)
    // ===========================================
    private String pensionDailyBizSymbol;
    private LocalDate pensionDailyJoinDate;
    private Long pensionDailyFee;     // 보험료 (입력 없으면 null → 0 처리)
    private Long pensionDailyPaid;    // 납부액 (입력 없으면 null → 0 처리)

    private String pensionRegularBizSymbol;
    private LocalDate pensionRegularJoinDate;
    private Long pensionRegularFee;
    private Long pensionRegularPaid;


    // ===========================================
    // 2. 건강보험 (Health Insurance)
    // ===========================================
    private String healthDailyBizSymbol;
    private LocalDate healthDailyJoinDate;
    private Long healthDailyFee;
    private Long healthDailyPaid;

    private String healthRegularBizSymbol;
    private LocalDate healthRegularJoinDate;
    private Long healthRegularFee;
    private Long healthRegularPaid;


    // ===========================================
    // 3. 고용보험 (Employment Insurance)
    // ===========================================
    private String employDailyMgmtNum;
    private LocalDate employDailyJoinDate;
    private Long employDailyFee;
    private Long employDailyPaid;

    private String employRegularMgmtNum;
    private LocalDate employRegularJoinDate;
    private Long employRegularFee;
    private Long employRegularPaid;


    // ===========================================
    // 4. 산재보험 (Industrial Accident Insurance)
    // ===========================================
    private String accidentDailyMgmtNum;
    private LocalDate accidentDailyJoinDate;
    private Long accidentDailyFee;
    private Long accidentDailyPaid;

    private String accidentRegularMgmtNum;
    private LocalDate accidentRegularJoinDate;
    private Long accidentRegularFee;
    private Long accidentRegularPaid;


    // ===========================================
    // 5. 퇴직공제 (Severance Deduction)
    // ===========================================
    private Boolean severanceTarget;          // true/false
    private String severanceDeductionNum;
    private LocalDate severanceJoinDate;

    private Integer dailyDeductionAmount;      // 일일 공제부금액
    private Long totalSeverancePaidAmount;     // 누적납부액



}