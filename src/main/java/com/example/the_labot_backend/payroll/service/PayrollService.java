package com.example.the_labot_backend.payroll.service;

import com.example.the_labot_backend.attendanceRecord.entity.AttendanceRecord;
import com.example.the_labot_backend.attendanceRecord.repository.AttendanceRecordRepository;
import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.global.exception.ForbiddenException;
import com.example.the_labot_backend.global.exception.NotFoundException;
import com.example.the_labot_backend.payroll.dto.*;
import com.example.the_labot_backend.payroll.entity.Payroll;
import com.example.the_labot_backend.payroll.repository.PayrollRepository;
import com.example.the_labot_backend.sites.entity.Site;
import com.example.the_labot_backend.sites.repository.SiteRepository;
import com.example.the_labot_backend.workers.entity.Worker;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final PayrollRepository payrollRepository;
    private final UserRepository userRepository;
    private final SiteRepository siteRepository;

    /**
     * 월별 근로자 임금 조회
     */
    @Transactional(readOnly = true)
    public List<PayrollTableResponse> getPayrollTable(Long siteId, int year, int month) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long adminId = Long.parseLong(auth.getName());

        validateSiteAccess(adminId,siteId);

        List<Payroll> payrolls =
                payrollRepository.findBySiteAndMonth(siteId, year, month);

        return payrolls.stream()
                .map(p -> convertToTableResponse(p, year, month))
                .toList();
    }

    /**
     * 월별 임금 생성
     */
    @Transactional
    public void generatePayrolls(Long siteId, int year, int month) {

        // 1. 권한 검증
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long adminId = Long.parseLong(auth.getName());

        validateSiteAccess(adminId,siteId);

        // 2. 이미 생성된 월인지 검사
        Long existingCount = payrollRepository.countBySiteAndYearMonth(siteId, year, month);
        if (existingCount != 0) {
            throw new ForbiddenException(
                    year + "년 " + month + "월 급여는 이미 생성되어 있습니다. 재생성할 수 없습니다."
            );
        }

        // 3. 해당 월 출역기록 조회
        List<AttendanceRecord> records =
                attendanceRecordRepository.findMonthlyRecordsBySite(siteId, year, month);

        if (records.isEmpty()) {
            return;
        }

        // 4. 근로자별로 그룹핑
        Map<Worker, List<AttendanceRecord>> grouped =
                records.stream().collect(Collectors.groupingBy(AttendanceRecord::getWorker));

        // 5. 각 근로자마다 Payroll 생성
        for (Map.Entry<Worker, List<AttendanceRecord>> entry : grouped.entrySet()) {

            Worker worker = entry.getKey();
            List<AttendanceRecord> workerRecords = entry.getValue();

            // 총공수 계산
            double totalManHour = workerRecords.stream()
                    .mapToDouble(AttendanceRecord::getActualTotalWork)
                    .sum();

            // 기준단가: 첫 기록의 unitPrice 사용
            Long unitPrice = workerRecords.get(0).getUnitPrice();

            // 지급합계 = 공수 × 기준단가 + 식대 등
            long calculatedTotalAmount =
                    Math.round(totalManHour * unitPrice);

            // 퇴직공제일수 = 근무일수
            int retirementDays = workerRecords.size();

            // 1. 기본 계산 대상 금액
            long totalAmount = calculatedTotalAmount;
            long monthlySalaryForInsurance = calculatedTotalAmount; // or worker.getMonthlySalaryForInsurance()

            // 2. 소득세 (일용직 단일세율 3%)
            long incomeTaxAuto          = Math.round(totalAmount * 0.03);
            long localIncomeTaxAuto     = Math.round(incomeTaxAuto * 0.10);
            long employmentAuto         = Math.round(totalAmount * 0.008);

            long nationalPensionAuto    = Math.round(monthlySalaryForInsurance * 0.045);
            long healthInsuranceAuto    = Math.round(monthlySalaryForInsurance * 0.03545);
            long longTermCareAuto       = Math.round(healthInsuranceAuto * 0.1295);

            // 4. 최종 값(final) 결정 규칙
            Long incomeTaxFinal              = incomeTaxAuto;
            Long localIncomeTaxFinal         = localIncomeTaxAuto;

            Long employmentFinal             = 0L;
            Long nationalPensionFinal        = 0L;
            Long healthInsuranceFinal        = 0L;
            Long longTermCareFinal           = 0L;

            // 8. 공제합계
            long totalDeductions =
                    incomeTaxFinal +
                            localIncomeTaxFinal +
                            employmentFinal +
                            nationalPensionFinal +
                            healthInsuranceFinal +
                            longTermCareFinal;

            // 9. 실지급액
            long netPay = totalAmount - totalDeductions;

            Payroll payroll = Payroll.builder()
                    .worker(worker)
                    .site(worker.getUser().getSite())
                    .payDate(LocalDate.of(year, month, 1))

                    .totalAmount(totalAmount)
                    .mealAllowance(0L)
                    .monthlySalaryForInsurance(monthlySalaryForInsurance)

                    // 소득세
                    .incomeTaxAuto(incomeTaxAuto)
                    .incomeTaxManual(null)
                    .incomeTax(incomeTaxFinal)

                    // 지방소득세
                    .localIncomeTaxAuto(localIncomeTaxAuto)
                    .localIncomeTaxManual(null)
                    .localIncomeTax(localIncomeTaxFinal)

                    // 고용보험
                    .isEmploymentInsuranceApplicable(false)
                    .employmentInsuranceAuto(employmentAuto)
                    .employmentInsuranceManual(null)
                    .employmentInsuranceAmount(employmentFinal)

                    // 국민연금
                    .isNationalPensionApplicable(false)
                    .nationalPensionAuto(nationalPensionAuto)
                    .nationalPensionManual(null)
                    .nationalPensionAmount(nationalPensionFinal)

                    // 건강보험
                    .isHealthInsuranceApplicable(false)
                    .healthInsuranceAuto(healthInsuranceAuto)
                    .healthInsuranceManual(null)
                    .healthInsuranceAmount(healthInsuranceFinal)

                    // 요양보험
                    .isLongTermCareApplicable(false)
                    .longTermCareAuto(longTermCareAuto)
                    .longTermCareManual(null)
                    .longTermCareAmount(longTermCareFinal)

                    .totalDeductions(totalDeductions)
                    .netPay(netPay)

                    .isRetirementDeductionApplicable(true)
                    .retirementDeductionDays(retirementDays)

                    .reasonForLeaving(null)

                    .build();

            payrollRepository.save(payroll);
        }
    }

    /**
     * 보험 적용 여부 수정
     */
    @Transactional
    public PayrollInsuranceUpdateResponse updateInsurance(
            Long siteId,
            Long payrollId,
            PayrollInsuranceUpdateRequest req) {

        // 1. payroll 조회
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new NotFoundException("Payroll을 찾을 수 없습니다."));

        if(!(siteId.equals(payroll.getSite().getId()))) {
            throw new ForbiddenException("해당 임금에 접근할 권한이 없습니다.");
        }

        // 2. 권한 체크 (본사/현장관리자)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long adminId = Long.parseLong(auth.getName());

        validateSiteAccess(adminId,siteId);

        // 3. 적용 여부 업데이트
        payroll.setIsEmploymentInsuranceApplicable(req.getEmploymentInsurance());
        payroll.setIsNationalPensionApplicable(req.getNationalPension());
        payroll.setIsHealthInsuranceApplicable(req.getHealthInsurance());
        payroll.setIsLongTermCareApplicable(req.getLongTermCare());

        // 4. 재계산
        reCalInsurance(payroll);

        return PayrollInsuranceUpdateResponse.builder()
                .payrollId(payroll.getId())

                .employmentInsurance(payroll.getIsEmploymentInsuranceApplicable())
                .nationalPension(payroll.getIsNationalPensionApplicable())
                .healthInsurance(payroll.getIsHealthInsuranceApplicable())
                .longTermCare(payroll.getIsLongTermCareApplicable())

                .incomeTax(payroll.getIncomeTax())
                .localIncomeTax(payroll.getLocalIncomeTax())

                .employmentInsuranceAmount(payroll.getEmploymentInsuranceAmount())
                .nationalPensionAmount(payroll.getNationalPensionAmount())
                .healthInsuranceAmount(payroll.getHealthInsuranceAmount())
                .longTermCareAmount(payroll.getLongTermCareAmount())

                .totalDeductions(payroll.getTotalDeductions())
                .netPay(payroll.getNetPay())
                .build();
    }

    /**
     * 보험 수동 수정
     */
    @Transactional
    public PayrollManualUpdateResponse updateManualValues(
            Long siteId,
            Long payrollId,
            PayrollManualUpdateRequest req) {

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new NotFoundException("Payroll을 찾을 수 없습니다."));

        if(!(siteId.equals(payroll.getSite().getId()))) {
            throw new ForbiddenException("해당 임금에 접근할 권한이 없습니다.");
        }

        // 권한 체크
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long adminId = Long.parseLong(auth.getName());

        validateSiteAccess(adminId,siteId);

        // 식대
        if (req.getMealAllowance() != null) {
            payroll.setMealAllowance(req.getMealAllowance());
        } else{
            payroll.setMealAllowance(0L);
        }

        payroll.setTotalAmount(
                payroll.getMealAllowance() + payroll.getMonthlySalaryForInsurance()
        );

        // 소득세
        if (req.getIncomeTaxManual() != null) {
            payroll.setIncomeTaxManual(req.getIncomeTaxManual());
            payroll.setIncomeTax(req.getIncomeTaxManual());
        } else {
            // 입력값이 없으면 자동값 유지
            payroll.setIncomeTaxManual(0L);
            payroll.setIncomeTax(payroll.getIncomeTaxAuto());
        }

        // 지방소득세
        if (req.getLocalIncomeTaxManual() != null) {
            payroll.setLocalIncomeTaxManual(req.getLocalIncomeTaxManual());
            payroll.setLocalIncomeTax(req.getLocalIncomeTaxManual());
        } else {
            payroll.setLocalIncomeTaxManual(0L);
            payroll.setLocalIncomeTax(payroll.getLocalIncomeTaxAuto());
        }

        // 고용보험
        long employmentInsurance;

        if (payroll.getIsEmploymentInsuranceApplicable()) {
            if (req.getEmploymentInsuranceManual() != null) {
                payroll.setEmploymentInsuranceManual(req.getEmploymentInsuranceManual());
                employmentInsurance = req.getEmploymentInsuranceManual();
            } else {
                // 입력값이 없으면 자동값 유지
                payroll.setEmploymentInsuranceManual(0L);
                employmentInsurance = payroll.getEmploymentInsuranceAuto();
            }
        } else {
            // 체크 해제되어있으면 0
            employmentInsurance = 0L;
        }

        payroll.setEmploymentInsuranceAmount(employmentInsurance);

        // 국민연금보험
        long nationalPension;

        if (payroll.getIsNationalPensionApplicable()) {
            if (req.getNationalPensionManual() != null) {
                payroll.setNationalPensionManual(req.getNationalPensionManual());
                nationalPension = req.getNationalPensionManual();
            } else {
                payroll.setNationalPensionManual(0L);
                nationalPension = payroll.getNationalPensionAuto();
            }
        } else {
            nationalPension = 0L;
        }

        payroll.setNationalPensionAmount(nationalPension);

        // 건강보험
        long healthInsurance;

        if (payroll.getIsHealthInsuranceApplicable()) {
            if (req.getHealthInsuranceManual() != null) {
                payroll.setHealthInsuranceManual(req.getHealthInsuranceManual());
                healthInsurance = req.getHealthInsuranceManual();
            } else {
                payroll.setHealthInsuranceManual(0L);
                healthInsurance = payroll.getHealthInsuranceAuto();
            }
        } else {
            healthInsurance = 0L;
        }

        payroll.setHealthInsuranceAmount(healthInsurance);

        // 요양보험
        long longTermCare;

        if (payroll.getIsLongTermCareApplicable()) {
            if (req.getLongTermCareManual() != null) {
                payroll.setLongTermCareManual(req.getLongTermCareManual());
                longTermCare = req.getLongTermCareManual();
            } else {
                payroll.setLongTermCareManual(0L);
                longTermCare = payroll.getLongTermCareAuto();
            }
        } else {
            longTermCare = 0L;
        }

        payroll.setLongTermCareAmount(longTermCare);

        long totalDeductions =
                payroll.getIncomeTax() +
                        payroll.getLocalIncomeTax() +
                        payroll.getEmploymentInsuranceAmount() +
                        payroll.getNationalPensionAmount() +
                        payroll.getHealthInsuranceAmount() +
                        payroll.getLongTermCareAmount();

        payroll.setTotalDeductions(totalDeductions);
        payroll.setNetPay(payroll.getTotalAmount() - totalDeductions);

        return PayrollManualUpdateResponse.builder()
                .payrollId(payroll.getId())
                .mealAllowance(payroll.getMealAllowance())
                .totalAmount(payroll.getTotalAmount())
                .incomeTax(payroll.getIncomeTax())
                .localIncomeTax(payroll.getLocalIncomeTax())
                .employmentInsuranceAmount(payroll.getEmploymentInsuranceAmount())
                .nationalPensionAmount(payroll.getNationalPensionAmount())
                .healthInsuranceAmount(payroll.getHealthInsuranceAmount())
                .longTermCareAmount(payroll.getLongTermCareAmount())
                .totalDeductions(payroll.getTotalDeductions())
                .netPay(payroll.getNetPay())
                .build();
    }

    /**
     * 현장 권한 검사 메소드
     */
    private void validateSiteAccess(Long userId, Long siteId) {

        User admin = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. userId=" + userId));

        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new NotFoundException("현장을 찾을 수 없습니다. siteId=" + siteId));

        if (!admin.getHeadOffice().getId().equals(site.getHeadOffice().getId())) {
            throw new ForbiddenException("해당 현장에 접근할 권한이 없습니다.");
        }
    }

    /**
     * 체크표시에 따라 결정되는 보험 계산
     */
    private void reCalInsurance(Payroll payroll) {

        long totalAmount = payroll.getTotalAmount();
        long baseSalary = payroll.getMonthlySalaryForInsurance(); // 보수월액

        // 1. 소득세 = 지급총액 × 3%
        long incomeTax;
        if (payroll.getIncomeTaxManual() != null) {
            incomeTax = payroll.getIncomeTaxManual();
        }
        // 수동값 없으면 자동값 사용
        else {
            incomeTax = payroll.getIncomeTaxAuto();
        }

        // 2. 지방소득세 = 소득세 × 10%
        long localIncomeTax;
        // 수동값이 있다면 수동값 우선 사용
        if (payroll.getLocalIncomeTaxManual() != null) {
            localIncomeTax = payroll.getLocalIncomeTaxManual();
        }
        // 수동값 없으면 자동값 사용
        else {
            localIncomeTax = payroll.getLocalIncomeTaxAuto();
        }

        // 3. 고용보험
        long employmentInsurance;
        if (payroll.getIsEmploymentInsuranceApplicable()) {

            // 수동값이 있다면 수동값 우선 사용
            if (payroll.getEmploymentInsuranceManual() != null) {
                employmentInsurance = payroll.getEmploymentInsuranceManual();
            }
            // 수동값 없으면 자동값 사용
            else {
                employmentInsurance = payroll.getEmploymentInsuranceAuto();
            }

        } else {
            // 보험 적용 안 함 → 최종값은 0
            employmentInsurance = 0L;
        }

        // 4. 국민연금
        long nationalPension;
        if (payroll.getIsNationalPensionApplicable()) {

            if (payroll.getNationalPensionManual() != null) {
                nationalPension = payroll.getNationalPensionManual();
            } else {
                nationalPension = payroll.getNationalPensionAuto();
            }

        } else {
            nationalPension = 0L;
        }

        // 5. 건강보험
        long healthInsurance;
        if (payroll.getIsHealthInsuranceApplicable()) {

            if (payroll.getHealthInsuranceManual() != null) {
                healthInsurance = payroll.getHealthInsuranceManual();
            } else {
                healthInsurance = payroll.getHealthInsuranceAuto();
            }

        } else {
            healthInsurance = 0L;
        }

        // 6. 요양보험
        long longTermCare;
        if (payroll.getIsLongTermCareApplicable()) {

            if (payroll.getLongTermCareManual() != null) {
                longTermCare = payroll.getLongTermCareManual();
            } else {
                longTermCare = payroll.getLongTermCareAuto();
            }

        } else {
            longTermCare = 0L;
        }

        // 7. 공제합계
        long totalDeductions =
                incomeTax +
                        localIncomeTax +
                        employmentInsurance +
                        nationalPension +
                        healthInsurance +
                        longTermCare;

        // 8. 실지급액
        long netPay = totalAmount - totalDeductions;

        // payroll 값 반영
        payroll.setIncomeTax(incomeTax);
        payroll.setLocalIncomeTax(localIncomeTax);

        payroll.setEmploymentInsuranceAmount(employmentInsurance);
        payroll.setNationalPensionAmount(nationalPension);
        payroll.setHealthInsuranceAmount(healthInsurance);
        payroll.setLongTermCareAmount(longTermCare);

        payroll.setTotalDeductions(totalDeductions);
        payroll.setNetPay(netPay);
    }

    private PayrollTableResponse convertToTableResponse(Payroll payroll, int year, int month) {

        Worker worker = payroll.getWorker();

        // 해당 근로자의 월별 출역기록 조회 (총 공수/단가 계산용)
        List<AttendanceRecord> records =
                attendanceRecordRepository.findMonthlyRecords(worker.getId(), year, month);

        // 한 달 동안 일한 총 공수
        Double totalManHour = records.stream()
                .mapToDouble(AttendanceRecord::getActualTotalWork)
                .sum();

        Long unitPrice = records.isEmpty() ? 0L : records.getFirst().getUnitPrice();

        return PayrollTableResponse.builder()
                .payrollId(payroll.getId())

                .workerName(worker.getUser().getName())
                .birthDate((worker.getBirthDate()))

                .unitPrice(unitPrice)
                .totalManHour(totalManHour)
                .totalAmount(payroll.getTotalAmount())

                .mealAllowance(payroll.getMealAllowance())
                .monthlySalaryForInsurance(payroll.getMonthlySalaryForInsurance())

                .incomeTax(payroll.getIncomeTax())
                .localIncomeTax(payroll.getLocalIncomeTax())

                .isEmploymentInsuranceApplicable(payroll.getIsEmploymentInsuranceApplicable())
                .employmentInsuranceAmount(payroll.getEmploymentInsuranceAmount())

                .isNationalPensionApplicable(payroll.getIsNationalPensionApplicable())
                .nationalPensionAmount(payroll.getNationalPensionAmount())

                .isBasicLivelihoodRecipient(payroll.getIsBasicLivelihoodRecipient())

                .isHealthInsuranceApplicable(payroll.getIsHealthInsuranceApplicable())
                .healthInsuranceAmount(payroll.getHealthInsuranceAmount())

                .isLongTermCareApplicable(payroll.getIsLongTermCareApplicable())
                .longTermCareAmount(payroll.getLongTermCareAmount())

                .totalDeductions(payroll.getTotalDeductions())
                .netPay(payroll.getNetPay())

                .isRetirementDeductionApplicable(payroll.getIsRetirementDeductionApplicable())
                .retirementDeductionDays(payroll.getRetirementDeductionDays())

                .reasonForLeaving(payroll.getReasonForLeaving())
                .build();
    }

}
