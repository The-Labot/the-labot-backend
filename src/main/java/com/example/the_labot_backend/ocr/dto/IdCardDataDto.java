package com.example.the_labot_backend.ocr.dto;

import lombok.Data;

@Data
public class IdCardDataDto {
    private String name;                // 이름
    private String address;             // 주소
    private String residentIdNumber;    // 주민등록번호 (운전면허증/주민증 공통)
}
