package com.example.the_labot_backend.workers.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class WorkerMyPageUpdateRequest {
    private String address;         // 주소
    private LocalDate birthDate;    // 생년월일
    private String phoneNumber;     // 전화번호 (로그인 ID 겸용 주의)
    private String emergencyNumber; // 비상연락처

    // 2. 계좌 정보
    private String bankName;        // 은행명
    private String accountNumber;   // 계좌번호
    private String accountHolder;   // 예금주
}
