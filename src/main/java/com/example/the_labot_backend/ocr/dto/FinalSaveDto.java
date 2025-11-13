package com.example.the_labot_backend.ocr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FinalSaveDto {
    // --- (1) 신분증 정보 (3개만) ---
    private String name;
    private String address;
    private String residentIdNumber;

    // --- (2) 계약서 정보 ---
    private String jobType;
    private String salary;
    private String phoneNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate contractStartDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate contractEndDate;
    private String siteName;    // (Worker 엔티티의 siteName 필드용)
    private String nationality; // (Worker 엔티티의 nationality 필드용)
}
