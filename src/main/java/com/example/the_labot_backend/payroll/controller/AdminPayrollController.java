package com.example.the_labot_backend.payroll.controller;

import com.example.the_labot_backend.payroll.dto.*;
import com.example.the_labot_backend.payroll.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/sites/{siteId}/payrolls")
@RequiredArgsConstructor
public class AdminPayrollController {

    private final PayrollService payrollService;

    // 월별 근로자 리스트 조회
    @GetMapping()
    public ResponseEntity<?> getPayrollTable(
            @PathVariable Long siteId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        List<PayrollTableResponse> response =  payrollService.getPayrollTable(siteId, year, month);
        return ResponseEntity.ok(Map.of(
                        "status", 200,
                        "message", "월별 근로자 임금 조회 완료",
                        "data", response
                )
        );
    }

    // 월별 임금 조회
    @GetMapping("/{workerId}/{payrollId}")
    public ResponseEntity<?> getPayrollInsuranceDetail(
            @PathVariable Long siteId,
            @PathVariable Long workerId,
            @PathVariable Long payrollId
    ) {
        PayrollInsuranceResponse response =
                payrollService.getPayrollDetail(siteId, workerId, payrollId);

        return ResponseEntity.ok(
                Map.of(
                        "status", 200,
                        "message", "급여 보험·세금 상세 조회 완료",
                        "data", response
                )
        );
    }

    // 월별 급여 자동 생성
    @PostMapping("/create")
    public ResponseEntity<?> createPayroll(
            @PathVariable Long siteId,
            @RequestBody PayrollCreateRequest request
    ) {
        payrollService.createPayrolls(siteId,request);
        return ResponseEntity.ok(Map.of(
                        "status", 200,
                        "message", "월별 급여 자동 생성 완료"
                )
        );
    }

    // 월별 임금 삭제
    @PostMapping("/delete")
    public ResponseEntity<?> deletePayrolls(
            @RequestBody PayrollDeleteRequest request,
            @PathVariable Long siteId
    ) {
        payrollService.deletePayrolls(siteId, request);

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "급여 삭제 완료"
        ));
    }

    // 월별 임금 상세 수정
    @PatchMapping("/{payrollId}")
    public ResponseEntity<?> updatePayroll(
            @PathVariable Long siteId,
            @PathVariable Long payrollId,
            @RequestBody PayrollUpdateRequest request
    ) {
        payrollService.updatePayroll(siteId,payrollId, request);
        return ResponseEntity.ok(Map.of(
                        "status", 200,
                        "message", "임금 상세 수정 완료"
                )
        );
    }
}
