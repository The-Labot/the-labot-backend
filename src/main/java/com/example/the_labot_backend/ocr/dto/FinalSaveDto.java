package com.example.the_labot_backend.ocr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FinalSaveDto {
    private String name;
    private String address;
    private String residentIdNumber;

    // --- (2) 계약서 정보 ---
    private String jobType;
    private String contractType;
    private String payReceive;//임금받는날
    private String salary;
    private String phoneNumber;
    private String emergencyNumber; // 비상연락처

    private String siteName;
    private String nationality;

    private String bankName;      // 은행
    private String accountHolder; // 예금주
    private String accountNumber; // 계좌번호

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate contractStartDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate contractEndDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate wageStartDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate wageEndDate;
}
