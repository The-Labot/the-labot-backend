package com.example.the_labot_backend.sites.entity.embeddable;

import com.example.the_labot_backend.sites.entity.SeveranceType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDate;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteSocialIns {

    // ============================================================
    // 1. 국민연금 (National Pension)
    //  - 일용/상용: 사업장기호 + 성립일
    //  - 보험료/납부액/납부율: 통합
    // ============================================================
    @Column(length = 20)
    private String pensionDailyBizSymbol;   // 일용 사업장 기호
    private LocalDate pensionDailyJoinDate;

    @Column(length = 20)
    private String pensionRegularBizSymbol; // 상용 사업장 기호
    private LocalDate pensionRegularJoinDate;

    private Long pensionFee;     // 보험료
    private Long pensionPaid;    // 납부액
    private Double pensionRate;  // 납부율 (자동 계산)


    // ============================================================
    // 2. 건강보험 (Health Insurance)
    // ============================================================
    @Column(length = 20)
    private String healthDailyBizSymbol;
    private LocalDate healthDailyJoinDate;

    @Column(length = 20)
    private String healthRegularBizSymbol;
    private LocalDate healthRegularJoinDate;

    private Long healthFee;
    private Long healthPaid;
    private Double healthRate;


    // ============================================================
    // 3. 고용보험 (Employment Insurance)
    // ============================================================
    @Column(length = 20)
    private String employDailyMgmtNum;
    private LocalDate employDailyJoinDate;

    @Column(length = 20)
    private String employRegularMgmtNum;
    private LocalDate employRegularJoinDate;

    private Long employFee;
    private Long employPaid;
    private Double employRate;


    // ============================================================
    // 4. 산재보험 (Industrial Accident Insurance)
    // ============================================================
    @Column(length = 20)
    private String accidentDailyMgmtNum;
    private LocalDate accidentDailyJoinDate;

    @Column(length = 20)
    private String accidentRegularMgmtNum;
    private LocalDate accidentRegularJoinDate;

    private Long accidentFee;
    private Long accidentPaid;
    private Double accidentRate;


    // ============================================================
    // 5. 퇴직공제 (Severance Deduction)
    // ============================================================
    private boolean isSeveranceTarget; // 퇴직공제 의무 여부

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SeveranceType severanceType; // MANDATORY/OPTIONAL

    @Column(length = 20)
    private String severanceDeductionNum;

    private LocalDate severanceJoinDate;

    private Integer dailyDeductionAmount; // 1일 공제부금

    private Long totalSeverancePaidAmount;

    private Double severancePaymentRate;
}