package com.example.the_labot_backend.payroll.entity;

import com.example.the_labot_backend.sites.entity.Site;
import com.example.the_labot_backend.workers.entity.Worker;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ============================================================
    // 기본 정보
    // ============================================================

    /**
     * 급여 대상 근로자 ID (User/Worker FK)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id")
    private Worker worker;

    /**
     * 해당 급여가 속한 현장 ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Site site;

    /**
     * 급여 산정 기준일 또는 지급일
     */
    private LocalDate payDate;


    // ============================================================
    // 지급 항목 (Earned Income)
    // ============================================================

    /**
     * 지급 총액 (기본급 + 각종 수당 포함)
     * 과세/비과세 구분 전 총합
     */
    private Long totalAmount;

    /**
     * 식대(월 10만원 한도 비과세 적용 대상)
     */
    private Long mealAllowance;

    /**
     * 4대보험 산정용 보수월액 (Monthly Standard Salary)
     */
    private Long monthlySalaryForInsurance;


    // ============================================================
    // 세금 관련 (Taxes)
    // ============================================================

    /**
     * 소득세 (간이세액표 기준 자동 계산)
     */
    private Long incomeTax;          // 최종 적용값

    /**
     * 지방소득세 (소득세 × 10%)
     */
    private Long localIncomeTax;         // 최종 적용값

    // ============================================================
    // 4대 보험 (Calculated Insurance Amounts)
    // ============================================================


    /**
     * 고용보험료 (근로자 부담)
     * 지급액×0.9% 정도를 기준으로 자동 계산
     */
    private Boolean isEmploymentInsuranceApplicable;

    private Long employmentInsuranceAmount;    // 최종 값

    /**
     * 국민연금 보험료 (근로자 부담 4.5%)
     */

    private Boolean isNationalPensionApplicable;

    private Long nationalPensionAmount;

    /**
     * 건강보험료 (근로자 부담 약 3.545%)
     */

    private Boolean isHealthInsuranceApplicable;

    private Long healthInsuranceAmount;

    /**
     * 장기요양보험료 (건강보험료 × 장기요양률)
     */

    private Boolean isLongTermCareApplicable;

    private Long longTermCareAmount;

    // ============================================================
    // 공제 및 실지급액 (Deductions)
    // ============================================================

    /**
     * 공제 총합 (세금 + 4대보험 + 퇴직공제 등 모든 공제)
     */
    private Long totalDeductions;

    /**
     * 실지급액 (지급합계 - 공제합계)
     */
    private Long netPay;


    // ============================================================
    // 건설근로자 퇴직공제 (Construction Retirement Deduction)
    // ============================================================

    /**
     * 기초생활수급자 여부 (감면 규칙 적용 가능)
     */
    private Boolean isBasicLivelihoodRecipient;

    /**
     * 퇴직공제 적용 여부 (true 시 공제 발생)
     */
    private Boolean isRetirementDeductionApplicable;

    /**
     * 퇴직공제 일수
     * - 직접 입력하지 않음
     * - 근로내역(출역기록)에서 자동 계산되어 들어오는 필드
     */
    private Integer retirementDeductionDays;


    // ============================================================
    // 기타 정보
    // ============================================================

    /**
     * 이직 사유 (필요 시 신고/정산용)
     */
    private String reasonForLeaving;
}