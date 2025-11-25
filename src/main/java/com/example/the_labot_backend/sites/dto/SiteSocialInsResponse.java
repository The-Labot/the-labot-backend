package com.example.the_labot_backend.sites.dto;

import com.example.the_labot_backend.sites.entity.SeveranceType;
import com.example.the_labot_backend.sites.entity.embeddable.SiteSocialIns;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SiteSocialInsResponse {

    // 1. 국민연금 (National Pension)
    private String pensionDailyBizSymbol;
    private String pensionDailyJoinDate;

    private String pensionRegularBizSymbol;
    private String pensionRegularJoinDate;

    private Long pensionFee;
    private Long pensionPaid;
    private Double pensionRate;

    // 2. 건강보험 (Health Insurance)
    private String healthDailyBizSymbol;
    private String healthDailyJoinDate;

    private String healthRegularBizSymbol;
    private String healthRegularJoinDate;

    private Long healthFee;
    private Long healthPaid;
    private Double healthRate;

    // 3. 고용보험 (Employment Insurance)
    private String employDailyMgmtNum;
    private String employDailyJoinDate;

    private String employRegularMgmtNum;
    private String employRegularJoinDate;

    private Long employFee;
    private Long employPaid;
    private Double employRate;

    // 4. 산재보험 (Industrial Accident)
    private String accidentDailyMgmtNum;
    private String accidentDailyJoinDate;

    private String accidentRegularMgmtNum;
    private String accidentRegularJoinDate;

    private Long accidentFee;
    private Long accidentPaid;
    private Double accidentRate;

    // 5. 퇴직공제 (Severance)
    private boolean severanceTarget;
    private SeveranceType severanceType; // 퇴직공제 의무인지 임의인지
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

                .pensionRegularBizSymbol(ins.getPensionRegularBizSymbol())
                .pensionRegularJoinDate(dateToString(ins.getPensionRegularJoinDate()))

                .pensionFee(ins.getPensionFee())
                .pensionPaid(ins.getPensionPaid())
                .pensionRate(ins.getPensionRate())

                // 2. 건강보험
                .healthDailyBizSymbol(ins.getHealthDailyBizSymbol())
                .healthDailyJoinDate(dateToString(ins.getHealthDailyJoinDate()))

                .healthRegularBizSymbol(ins.getHealthRegularBizSymbol())
                .healthRegularJoinDate(dateToString(ins.getHealthRegularJoinDate()))

                .healthFee(ins.getHealthFee())
                .healthPaid(ins.getHealthPaid())
                .healthRate(ins.getHealthRate())

                // 3. 고용보험
                .employDailyMgmtNum(ins.getEmployDailyMgmtNum())
                .employDailyJoinDate(dateToString(ins.getEmployDailyJoinDate()))

                .employRegularMgmtNum(ins.getEmployRegularMgmtNum())
                .employRegularJoinDate(dateToString(ins.getEmployRegularJoinDate()))

                .employFee(ins.getEmployFee())
                .employPaid(ins.getEmployPaid())
                .employRate(ins.getEmployRate())

                // 4. 산재보험
                .accidentDailyMgmtNum(ins.getAccidentDailyMgmtNum())
                .accidentDailyJoinDate(dateToString(ins.getAccidentDailyJoinDate()))

                .accidentRegularMgmtNum(ins.getAccidentRegularMgmtNum())
                .accidentRegularJoinDate(dateToString(ins.getAccidentRegularJoinDate()))

                .accidentFee(ins.getAccidentFee())
                .accidentPaid(ins.getAccidentPaid())
                .accidentRate(ins.getAccidentRate())

                // 5. 퇴직공제
                .severanceTarget(ins.isSeveranceTarget())
                .severanceType(ins.getSeveranceType())
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
