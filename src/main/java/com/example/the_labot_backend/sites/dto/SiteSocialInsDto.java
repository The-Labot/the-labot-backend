package com.example.the_labot_backend.sites.dto;

import com.example.the_labot_backend.sites.entity.SeveranceType;
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

    private String pensionRegularBizSymbol;
    private LocalDate pensionRegularJoinDate;

    private Long pensionFee;     // 보험료 (입력 없으면 null → 0 처리)
    private Long pensionPaid;    // 납부액 (입력 없으면 null → 0 처리)


    // ===========================================
    // 2. 건강보험 (Health Insurance)
    // ===========================================
    private String healthDailyBizSymbol;
    private LocalDate healthDailyJoinDate;

    private String healthRegularBizSymbol;
    private LocalDate healthRegularJoinDate;

    private Long healthFee;
    private Long healthPaid;


    // ===========================================
    // 3. 고용보험 (Employment Insurance)
    // ===========================================
    private String employDailyMgmtNum;
    private LocalDate employDailyJoinDate;

    private String employRegularMgmtNum;
    private LocalDate employRegularJoinDate;

    private Long employFee;
    private Long employPaid;


    // ===========================================
    // 4. 산재보험 (Industrial Accident Insurance)
    // ===========================================
    private String accidentDailyMgmtNum;
    private LocalDate accidentDailyJoinDate;

    private String accidentRegularMgmtNum;
    private LocalDate accidentRegularJoinDate;

    private Long accidentFee;
    private Long accidentPaid;


    // ===========================================
    // 5. 퇴직공제 (Severance Deduction)
    // ===========================================
    private Boolean severanceTarget;          // true/false
    private SeveranceType severanceType; // 퇴직공제 의무인지 임의인지
    private String severanceDeductionNum;
    private LocalDate severanceJoinDate;

    private Integer dailyDeductionAmount;      // 일일 공제부금액
    private Long totalSeverancePaidAmount;     // 누적납부액



}