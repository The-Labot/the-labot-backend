package com.example.the_labot_backend.files.entity;

/**
 * 파일이 어떤 도메인에 속하는지 구분하는 분류값.
 * 모든 파일이 file 테이블 하나에 저장되므로 (targetType, targetId) 조합으로 소유자를 찾는다.
 * DB에는 상수명이 그대로 저장된다. (@Enumerated(EnumType.STRING))
 */
public enum FileTargetType {

    NOTICE,                 // 공지사항 첨부
    HAZARD,                 // 위험요소 신고 사진
    REPORT,                 // 작업일보 첨부
    SITE_MAP,               // 현장 지도

    EDUCATION_MATERIAL,     // 안전교육 자료
    EDUCATION_PHOTO,        // 안전교육 사진
    EDUCATION_SIGNATURE,    // 안전교육 서명

    WORKER_CONTRACT,        // 근로계약서
    WORKER_LICENSE,         // 자격증
    WORKER_PAYROLL;         // 급여명세서

    /** 근로자 개인 문서 여부. 문자열 접두사 비교(startsWith("WORKER")) 대체용 */
    public boolean isWorkerDocument() {
        return name().startsWith("WORKER_");
    }
}
