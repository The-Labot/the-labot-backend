package com.example.the_labot_backend.ocr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ContractDataDto {

    private String contractType; //월급제 or일급제
    private String jobType;         // 직종
    private String salary;          // 급여
    private String payReceive;//임금 받는날
    private String siteName;        // 현장명
    private String bankName;        // 은행명
    private String accountHolder;   // 예금주
    private String accountNumber;   // 계좌번호

    private String phoneNumber;     // 본인 연락처
    private String emergencyNumber; // 비상 연락처   // 연락처




    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate contractStartDate; // 근로 시작일

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate contractEndDate;   // 근로 종료일

    // ★ 월정제/일반 구분을 위해 추가된 필드
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate wageStartDate;      // 급여 산정 시작일

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate wageEndDate;        // 급여 산정 종료일
}
