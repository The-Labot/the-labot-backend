package com.example.the_labot_backend.sites.dto;

import com.example.the_labot_backend.sites.entity.embeddable.SiteSocialIns;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SiteSocialInsResponse {

    // 1. 국민연금 (National Pension)
    private String pensionDailyBizSymbol;
    private String pensionDailyJoinDate;
    private Long pensionDailyFee;
    private Long pensionDailyPaid;
    private Double pensionDailyRate;

    private String pensionRegularBizSymbol;
    private String pensionRegularJoinDate;
    private Long pensionRegularFee;
    private Long pensionRegularPaid;
    private Double pensionRegularRate;

    // 2. 건강보험 (Health Insurance)
    private String healthDailyBizSymbol;
    private String healthDailyJoinDate;
    private Long healthDailyFee;
    private Long healthDailyPaid;
    private Double healthDailyRate;

    private String healthRegularBizSymbol;
    private String healthRegularJoinDate;
    private Long healthRegularFee;
    private Long healthRegularPaid;
    private Double healthRegularRate;

    // 3. 고용보험 (Employment Insurance)
    private String employDailyMgmtNum;
    private String employDailyJoinDate;
    private Long employDailyFee;
    private Long employDailyPaid;
    private Double employDailyRate;

    private String employRegularMgmtNum;
    private String employRegularJoinDate;
    private Long employRegularFee;
    private Long employRegularPaid;
    private Double employRegularRate;

    // 4. 산재보험 (Industrial Accident)
    private String accidentDailyMgmtNum;
    private String accidentDailyJoinDate;
    private Long accidentDailyFee;
    private Long accidentDailyPaid;
    private Double accidentDailyRate;

    private String accidentRegularMgmtNum;
    private String accidentRegularJoinDate;
    private Long accidentRegularFee;
    private Long accidentRegularPaid;
    private Double accidentRegularRate;

    // 5. 퇴직공제 (Severance)
    private boolean severanceTarget;
    private String severanceDeductionNum;
    private String severanceJoinDate;
    private Integer dailyDeductionAmount;
    private Long totalSeverancePaidAmount;
    private Double severancePaymentRate;


    // ======================
    //  Entity → Response
    // ======================
    public static SiteSocialInsResponse from(SiteSocialIns ins) {
        if (ins == null) return null;

        return SiteSocialInsResponse.builder()

                // 1. 국민연금
                .pensionDailyBizSymbol(ins.getPensionDailyBizSymbol())
                .pensionDailyJoinDate(dateToString(ins.getPensionDailyJoinDate()))
                .pensionDailyFee(ins.getPensionDailyFee())
                .pensionDailyPaid(ins.getPensionDailyPaid())
                .pensionDailyRate(ins.getPensionDailyRate())

                .pensionRegularBizSymbol(ins.getPensionRegularBizSymbol())
                .pensionRegularJoinDate(dateToString(ins.getPensionRegularJoinDate()))
                .pensionRegularFee(ins.getPensionRegularFee())
                .pensionRegularPaid(ins.getPensionRegularPaid())
                .pensionRegularRate(ins.getPensionRegularRate())

                // 2. 건강보험
                .healthDailyBizSymbol(ins.getHealthDailyBizSymbol())
                .healthDailyJoinDate(dateToString(ins.getHealthDailyJoinDate()))
                .healthDailyFee(ins.getHealthDailyFee())
                .healthDailyPaid(ins.getHealthDailyPaid())
                .healthDailyRate(ins.getHealthDailyRate())

                .healthRegularBizSymbol(ins.getHealthRegularBizSymbol())
                .healthRegularJoinDate(dateToString(ins.getHealthRegularJoinDate()))
                .healthRegularFee(ins.getHealthRegularFee())
                .healthRegularPaid(ins.getHealthRegularPaid())
                .healthRegularRate(ins.getHealthRegularRate())

                // 3. 고용보험
                .employDailyMgmtNum(ins.getEmployDailyMgmtNum())
                .employDailyJoinDate(dateToString(ins.getEmployDailyJoinDate()))
                .employDailyFee(ins.getEmployDailyFee())
                .employDailyPaid(ins.getEmployDailyPaid())
                .employDailyRate(ins.getEmployDailyRate())

                .employRegularMgmtNum(ins.getEmployRegularMgmtNum())
                .employRegularJoinDate(dateToString(ins.getEmployRegularJoinDate()))
                .employRegularFee(ins.getEmployRegularFee())
                .employRegularPaid(ins.getEmployRegularPaid())
                .employRegularRate(ins.getEmployRegularRate())

                // 4. 산재보험
                .accidentDailyMgmtNum(ins.getAccidentDailyMgmtNum())
                .accidentDailyJoinDate(dateToString(ins.getAccidentDailyJoinDate()))
                .accidentDailyFee(ins.getAccidentDailyFee())
                .accidentDailyPaid(ins.getAccidentDailyPaid())
                .accidentDailyRate(ins.getAccidentDailyRate())

                .accidentRegularMgmtNum(ins.getAccidentRegularMgmtNum())
                .accidentRegularJoinDate(dateToString(ins.getAccidentRegularJoinDate()))
                .accidentRegularFee(ins.getAccidentRegularFee())
                .accidentRegularPaid(ins.getAccidentRegularPaid())
                .accidentRegularRate(ins.getAccidentRegularRate())

                // 5. 퇴직공제
                .severanceTarget(ins.isSeveranceTarget())
                .severanceDeductionNum(ins.getSeveranceDeductionNum())
                .severanceJoinDate(dateToString(ins.getSeveranceJoinDate()))
                .dailyDeductionAmount(ins.getDailyDeductionAmount())
                .totalSeverancePaidAmount(ins.getTotalSeverancePaidAmount())
                .severancePaymentRate(ins.getSeverancePaymentRate())

                .build();
    }


    private static String dateToString(java.time.LocalDate date) {
        return (date == null) ? null : date.toString();
    }
}
