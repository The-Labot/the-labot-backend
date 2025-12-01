package com.example.the_labot_backend.payroll.service;

import com.example.the_labot_backend.attendanceRecord.entity.AttendanceRecord;
import com.example.the_labot_backend.attendanceRecord.repository.AttendanceRecordRepository;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.global.exception.BadRequestException;
import com.example.the_labot_backend.global.exception.ForbiddenException;
import com.example.the_labot_backend.global.exception.NotFoundException;
import com.example.the_labot_backend.payroll.dto.*;
import com.example.the_labot_backend.payroll.entity.Payroll;
import com.example.the_labot_backend.payroll.repository.PayrollRepository;
import com.example.the_labot_backend.sites.repository.SiteRepository;
import com.example.the_labot_backend.workers.entity.Worker;
import com.example.the_labot_backend.workers.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final PayrollRepository payrollRepository;
    private final UserRepository userRepository;
    private final SiteRepository siteRepository;
    private final WorkerRepository workerRepository;

    /**
     * 월별 근로자 리스트 조회
     */
    @Transactional(readOnly = true)
    public List<PayrollTableResponse> getPayrollTable(Long siteId, int year, int month) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long adminId = Long.parseLong(auth.getName());

        List<Worker> workers =
                workerRepository.findWorkersBySiteId(siteId);

        List<Payroll> payrolls =
                payrollRepository.findBySiteAndMonth(siteId, year, month);

        // 3) workerId -> payroll 매핑
        Map<Long, Payroll> payrollMap = payrolls.stream()
                .collect(Collectors.toMap(
                        p -> p.getWorker().getId(),
                        p -> p
                ));

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<PayrollTableResponse> result = new ArrayList<>();

        for (Worker w : workers) {

            Payroll p = payrollMap.get(w.getId());

            // ★ 월별 총 공수 계산 (일별 manHour 합계)
            Double totalManHour =
                    attendanceRecordRepository.getMonthlyTotalManHour(w.getId(), start, end);

            Long unitPrice = Long.parseLong(w.getSalary().replaceAll(",", ""));

            PayrollTableResponse dto = PayrollTableResponse.builder()
                    .payrollId(p != null ? p.getId() : null)
                    .workerId(w.getId())
                    .workerName(w.getUser().getName())
                    .birthDate(w.getBirthDate())
                    .wageType(w.getContractType())
                    .unitPrice(unitPrice)              // 근로자 기본 단가
                    .totalManHour(totalManHour)        // 총 공수
                    .totalAmount(p != null ? p.getTotalAmount() : null) // 총 지급액
                    .totalDeductions(p != null ? p.getTotalDeductions() : null)
                    .netPay(p != null ? p.getNetPay() : null)
                    .build();

            result.add(dto);
        }

        return result;
    }

    /**
     * 근로자 임금 상세 조회
     */
    @Transactional(readOnly = true)
    public PayrollInsuranceResponse getPayrollDetail(Long siteId, Long workerId, Long payrollId) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new NotFoundException("근로자를 찾을 수 없습니다."));
        if (!worker.getUser().getSite().getId().equals(siteId)) {
            throw new ForbiddenException("해당 현장의 근로자가 아닙니다.");
        }

        // 2) Payroll 검증
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new NotFoundException("급여 내역을 찾을 수 없습니다."));

        if (!payroll.getWorker().getId().equals(workerId)) {
            throw new ForbiddenException("해당 근로자의 급여 내역이 아닙니다.");
        }

        return PayrollInsuranceResponse.builder()
                .payrollId(payroll.getId())
                .totalAmount(payroll.getTotalAmount())
                .mealAllowance(payroll.getMealAllowance())
                .monthlySalaryForInsurance(payroll.getMonthlySalaryForInsurance())

                .incomeTax(payroll.getIncomeTax())
                .localIncomeTax(payroll.getLocalIncomeTax())

                .isEmploymentInsuranceApplicable(payroll.getIsEmploymentInsuranceApplicable())
                .employmentInsuranceAmount(payroll.getEmploymentInsuranceAmount())

                .isNationalPensionApplicable(payroll.getIsNationalPensionApplicable())
                .nationalPensionAmount(payroll.getNationalPensionAmount())

                .isHealthInsuranceApplicable(payroll.getIsHealthInsuranceApplicable())
                .healthInsuranceAmount(payroll.getHealthInsuranceAmount())

                .isLongTermCareApplicable(payroll.getIsLongTermCareApplicable())
                .longTermCareAmount(payroll.getLongTermCareAmount())

                .isBasicLivelihoodRecipient(payroll.getIsBasicLivelihoodRecipient())

                .totalDeductions(payroll.getTotalDeductions())
                .netPay(payroll.getNetPay())

                .isRetirementDeductionApplicable(payroll.getIsRetirementDeductionApplicable())
                .retirementDeductionDays(payroll.getRetirementDeductionDays())

                .reasonForLeaving(payroll.getReasonForLeaving())
                .build();
    }

    /**
     * 월별 임금 생성
     */
    @Transactional
    public void createPayrolls(Long siteId, PayrollCreateRequest request) {

        // 1. 권한 검증
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long adminId = Long.parseLong(auth.getName());

        int year = request.getYear();
        int month = request.getMonth();
        List<Long> workerIds = request.getWorkerIds();

        if (workerIds == null || workerIds.isEmpty()) {
            throw new BadRequestException("임금을 생성할 근로자가 없습니다.");
        }

        for (Long workerId : workerIds) {

            Worker worker = workerRepository.findById(workerId)
                    .orElseThrow(() -> new NotFoundException("근로자를 찾을 수 없습니다. ID=" + workerId));

            // 1) 현장 소속 검증
            if (!worker.getUser().getSite().getId().equals(siteId)) {
                throw new ForbiddenException("해당 근로자는 이 현장에 소속되지 않습니다. ID=" + workerId);
            }

            // 2) 이미 해당 월의 임금이 생성되었는지 확인
            Optional<Payroll> existing = payrollRepository.findByWorkerAndYearAndMonth(workerId, year, month);
            if (existing.isPresent()) {
                throw new BadRequestException(
                        String.format("%s년 %s월 임금이 이미 존재합니다. workerId=%d", year, month, workerId)
                );
            }

            // 3) 출석 기록을 기반으로 월 총 공수 계산
            List<AttendanceRecord> records =
                    attendanceRecordRepository.findMonthlyRecords(workerId, year, month);

            double totalManHour = records.stream()
                    .mapToDouble(AttendanceRecord::getManHour)
                    .sum();

            // 4) 지급 총액 = 단가 × 공수
            long unitPrice = Long.parseLong(worker.getSalary()); // salary가 string이라 했음
            long totalAmount = Math.round(unitPrice * totalManHour);

            // 지급합계 = 공수 × 기준단가 + 식대 등
            long calculatedTotalAmount =
                    Math.round(totalManHour * unitPrice);

            // 퇴직공제일수 = 근무일수
            int retirementDays = records.size();

            // 2. 소득세 (일용직 단일세율 3%)
            long incomeTax = Math.round(totalAmount * 0.03);
            long localIncomeTax = Math.round(incomeTax * 0.10);
            long employment = Math.round(totalAmount * 0.008);

            long nationalPension = Math.round(calculatedTotalAmount * 0.045);
            long healthInsurance = Math.round(calculatedTotalAmount * 0.03545);
            long longTermCare = Math.round(healthInsurance * 0.1295);

            // 8. 공제합계
            long totalDeductions = incomeTax + localIncomeTax;

            // 9. 실지급액
            long netPay = totalAmount - totalDeductions;

            Payroll payroll = Payroll.builder()
                    .worker(worker)
                    .site(worker.getUser().getSite())
                    .payDate(LocalDate.of(year, month, 1))

                    .totalAmount(totalAmount)
                    .mealAllowance(0L)
                    .monthlySalaryForInsurance(calculatedTotalAmount)

                    // 소득세
                    .incomeTax(incomeTax)

                    // 지방소득세
                    .localIncomeTax(localIncomeTax)

                    // 고용보험
                    .isEmploymentInsuranceApplicable(false)
                    .employmentInsuranceAmount(employment)

                    // 국민연금
                    .isNationalPensionApplicable(false)
                    .nationalPensionAmount(nationalPension)

                    // 건강보험
                    .isHealthInsuranceApplicable(false)
                    .healthInsuranceAmount(healthInsurance)

                    // 요양보험
                    .isLongTermCareApplicable(false)
                    .longTermCareAmount(longTermCare)

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
     * 임금 수정
     */
    @Transactional
    public void updatePayroll(
            Long siteId,
            Long payrollId,
            PayrollUpdateRequest req) {

        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new NotFoundException("급여 내역을 찾을 수 없습니다."));

        if (!payroll.getSite().getId().equals(siteId)) {
            throw new ForbiddenException("접근 할 수 없는 임금 내역입니다.");
        }

        // ============================
        // 1) 식대
        // ============================
        payroll.setMealAllowance(req.getMealAllowance());

        // ============================
        // 2) 세금
        // ============================
        payroll.setIncomeTax(req.getIncomeTax());

        payroll.setLocalIncomeTax(req.getLocalIncomeTax());

        // ============================
        // 3) 고용보험
        // ============================
        payroll.setIsEmploymentInsuranceApplicable(req.getIsEmploymentInsuranceApplicable());

        payroll.setEmploymentInsuranceAmount(req.getEmploymentInsurance());

        // ============================
        // 4) 국민연금
        // ============================
        payroll.setIsNationalPensionApplicable(req.getIsNationalPensionApplicable());

        payroll.setNationalPensionAmount(req.getNationalPension());

        // ============================
        // 5) 건강보험
        // ============================
        payroll.setIsHealthInsuranceApplicable(req.getIsHealthInsuranceApplicable());

        payroll.setHealthInsuranceAmount(req.getHealthInsurance());

        // ============================
        // 6) 장기요양보험
        // ============================
        payroll.setIsLongTermCareApplicable(req.getIsLongTermCareApplicable());

        payroll.setLongTermCareAmount(req.getLongTermCare());

        // ============================
        // 7) 퇴직공제 + 퇴사 사유
        // ============================
        payroll.setIsRetirementDeductionApplicable(req.getIsRetirementDeductionApplicable());

        payroll.setReasonForLeaving(req.getReasonForLeaving());


        // ============================
        // 8) 공제 총합 & 실지급액 재계산
        // ============================
        long totalDeductions =
                payroll.getIncomeTax()
                        + payroll.getLocalIncomeTax()
                        + (payroll.getIsEmploymentInsuranceApplicable()
                        ? payroll.getEmploymentInsuranceAmount() : 0)
                        + (payroll.getIsNationalPensionApplicable()
                        ? payroll.getNationalPensionAmount() : 0)
                        + (payroll.getIsHealthInsuranceApplicable()
                        ? payroll.getHealthInsuranceAmount() : 0)
                        + (payroll.getIsLongTermCareApplicable()
                        ? payroll.getLongTermCareAmount() : 0);

        payroll.setTotalDeductions(totalDeductions);

        payroll.setNetPay(payroll.getTotalAmount() - totalDeductions);

        // 변경 감지로 자동 저장됨 (JPA)
    }

    /**
            * 임금 삭제
     */
    @Transactional
    public void deletePayrolls(Long siteId, PayrollDeleteRequest request) {

        int year = request.getYear();
        int month = request.getMonth();
        List<Long> workerIds = request.getWorkerIds();

        if (workerIds == null || workerIds.isEmpty()) {
            throw new BadRequestException("삭제할 근로자가 없습니다.");
        }

        for (Long workerId : workerIds) {

            Payroll payroll = payrollRepository
                    .findByWorkerAndYearAndMonth(workerId, year, month)
                    .orElseThrow(() ->
                            new NotFoundException("해당 근로자의 "+year+"-"+month+" 급여가 존재하지 않습니다. workerId=" + workerId)
                    );

            // 사이트 검증
            if (!payroll.getWorker().getUser().getSite().getId().equals(siteId)) {
                throw new ForbiddenException("해당 근로자의 급여는 이 현장에 속하지 않습니다. workerId=" + workerId);
            }

            payrollRepository.delete(payroll);
        }
    }




}
