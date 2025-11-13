package com.example.the_labot_backend.ocr.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ContractDataDto {
    /**
     *ocr에서 받아온 json을 우리가 보기 편한 형태로 바꾼후에 여기에 있는 변수에 담는다
     */
    private String jobType;         // 직종
    private String salary;          // 급여

    private String phoneNumber;     // 연락처


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") //계약 시작일
    private LocalDate contractStartDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") //계약 종료일
    private LocalDate contractEndDate;
}
