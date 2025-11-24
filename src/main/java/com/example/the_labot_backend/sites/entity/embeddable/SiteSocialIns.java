package com.example.the_labot_backend.sites.entity.embeddable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
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
    // ============================================================
    // [일용직 - Daily]
    @Column(length = 20)
    private String pensionDailyBizSymbol;   // 국민연금 일용 사업장 기호
    private LocalDate pensionDailyJoinDate; // 성립일
    private Long pensionDailyFee;           // 보험료
    private Long pensionDailyPaid;          // 납부액
    private Double pensionDailyRate;        // 납부율

    // [상용직 - Regular]
    @Column(length = 20)
    private String pensionRegularBizSymbol; // 국민연금 상용 사업장 기호
    private LocalDate pensionRegularJoinDate;
    private Long pensionRegularFee;
    private Long pensionRegularPaid;
    private Double pensionRegularRate;


    // ============================================================
    // 2. 건강보험 (Health Insurance)
    // ============================================================
    // [일용직 - Daily]
    @Column(length = 20)
    private String healthDailyBizSymbol;    // 건강보험 일용 사업장 기호
    private LocalDate healthDailyJoinDate;
    private Long healthDailyFee;
    private Long healthDailyPaid;
    private Double healthDailyRate;

    // [상용직 - Regular]
    @Column(length = 20)
    private String healthRegularBizSymbol;  // 건강보험 상용 사업장 기호
    private LocalDate healthRegularJoinDate;
    private Long healthRegularFee;
    private Long healthRegularPaid;
    private Double healthRegularRate;


    // ============================================================
    // 3. 고용보험 (Employment Insurance)
    // ============================================================
    // [일용직 - Daily]
    @Column(length = 20)
    private String employDailyMgmtNum;      // 고용보험 일용 관리번호
    private LocalDate employDailyJoinDate;
    private Long employDailyFee;
    private Long employDailyPaid;
    private Double employDailyRate;

    // [상용직 - Regular]
    @Column(length = 20)
    private String employRegularMgmtNum;    // 고용보험 상용 관리번호
    private LocalDate employRegularJoinDate;
    private Long employRegularFee;
    private Long employRegularPaid;
    private Double employRegularRate;


    // ============================================================
    // 4. 산재보험 (Industrial Accident Insurance)
    // ============================================================
    // [일용직 - Daily]
    @Column(length = 20)
    private String accidentDailyMgmtNum;    // 산재보험 일용 관리번호
    private LocalDate accidentDailyJoinDate;
    private Long accidentDailyFee;
    private Long accidentDailyPaid;
    private Double accidentDailyRate;

    // [상용직 - Regular]
    @Column(length = 20)
    private String accidentRegularMgmtNum;  // 산재보험 상용 관리번호
    private LocalDate accidentRegularJoinDate;
    private Long accidentRegularFee;
    private Long accidentRegularPaid;
    private Double accidentRegularRate;


    // ============================================================
    // 5. 퇴직공제 (Severance Deduction) - 구조 다름 (단일 관리)
    // ============================================================
    private boolean isSeveranceTarget; // 퇴직공제 가입 의무 여부 (Check)

    @Column(length = 20)
    private String severanceDeductionNum; // 퇴직공제번호

    private LocalDate severanceJoinDate; // 성립일

    private Integer dailyDeductionAmount; // 1일 공제부금액 (예: 6000원)

    private Long totalSeverancePaidAmount; // 누적 납부액

    private Double severancePaymentRate; // 누적 납부율
}