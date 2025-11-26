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

    // 월별 근로자 임금 조회
    @GetMapping()
    public ResponseEntity<?> getPayrollTable(
            @PathVariable Long siteId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        List<PayrollTableResponse> response =  payrollService.getPayrollTable(siteId, year, month);
        return ResponseEntity.ok(Map.of(
                        "status", 200,
                        "message", "월별 근로자 임금 조회 성공",
                        "data", response
                )
        );
    }

    // 월별 급여 자동 생성
    @PostMapping("/generate")
    public ResponseEntity<?> generate(
            @PathVariable Long siteId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        payrollService.generatePayrolls(siteId,year,month);
        return ResponseEntity.ok(Map.of(
                        "status", 200,
                        "message", "월별 급여 자동 생성 성공"
                )
        );
    }

    // 보험 적용
    @PatchMapping("/{payrollId}/insurance")
    public ResponseEntity<?> updateInsurance(
            @PathVariable Long siteId,
            @PathVariable Long payrollId,
            @RequestBody PayrollInsuranceUpdateRequest request
    ) {
        PayrollInsuranceUpdateResponse response = payrollService.updateInsurance(siteId,payrollId, request);
        return ResponseEntity.ok(Map.of(
                        "status", 200,
                        "message", "보험 적용 성공",
                        "data", response
                )
        );
    }

    @PatchMapping("/{payrollId}/manual")
    public ResponseEntity<?> updateManual(
            @PathVariable Long siteId,
            @PathVariable Long payrollId,
            @RequestBody PayrollManualUpdateRequest request
    ) {

        PayrollManualUpdateResponse response = payrollService.updateManualValues(siteId,payrollId, request);
        return ResponseEntity.ok(Map.of(
                        "status", 200,
                        "message", "보험 적용 성공",
                        "data", response
                )
        );
    }


}
