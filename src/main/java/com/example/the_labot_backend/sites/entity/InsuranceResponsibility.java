package com.example.the_labot_backend.sites.entity;

public enum InsuranceResponsibility {
    NONE,               // 해당없음 (미승인 - 원청 납부)
    ALL,                // 고용+산재 (둘 다 하청인 우리가 납부)
    EMPLOYMENT_ONLY,    // 고용만 승인
    ACCIDENT_ONLY       // 산재만 승인
}