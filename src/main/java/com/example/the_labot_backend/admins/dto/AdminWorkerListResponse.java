package com.example.the_labot_backend.admins.dto;

import com.example.the_labot_backend.workers.entity.Worker;
import com.example.the_labot_backend.workers.entity.WorkerStatus;
import com.example.the_labot_backend.workers.entity.embeddable.WorkerBankAccount;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class AdminWorkerListResponse {
    private Long workerId;          // 근로자 ID
    private String name;            // 근로자 이름
    private String siteName;        // [★] 소속 현장명
    private String position;        // 직종
    private WorkerStatus status;    // 상태 (근무중/퇴직 등)
    private String phone;           // 전화번호

    // 표에 보여줄 상세 정보들
    private String contractType;    // 계약 형태
    private LocalDate wageStartDate;// 근무 시작일
    private LocalDate wageEndDate;  // 근무 종료일
    private String salary;          // 급여
    // --- [★ 추가 요청 필드] ---
    private LocalDate birthDate;       // 생년월일
    private String gender;             // 성별
    private String address;            // 주소
    private String emergencyNumber;    // 비상연락처
    private String payReceive;         // 임금 받는 날
    private LocalDate contractStartDate; // 계약 시작일
    private LocalDate contractEndDate;   // 계약 종료일
    // [★] 은행 정보 (WorkerBankAccount 내장 타입에서 추출)
    private String bankName;
    private String accountNumber;
    private String accountHolder;

    public static AdminWorkerListResponse from(Worker worker) {
        // 1. 은행 정보 안전하게 추출 (Null 체크)
        String bankName = null;
        String accountNum = null;
        String holder = null;
        if (worker.getBankAccount() != null) {
            WorkerBankAccount account = worker.getBankAccount();
            bankName = account.getBankName();
            accountNum = account.getAccountNumber();
            holder = account.getAccountHolder();
        }

        return AdminWorkerListResponse.builder()
                .workerId(worker.getId())
                .name(worker.getUser().getName())


                .siteName(worker.getSiteName()) //site에서 projectName가지고옴

                .position(worker.getPosition())
                .status(worker.getStatus())
                .phone(worker.getUser().getPhoneNumber())
                .contractType(worker.getContractType())
                .wageStartDate(worker.getWageStartDate())
                .wageEndDate(worker.getWageEndDate())
                .salary(worker.getSalary())
                // [★ 추가 매핑]
                .birthDate(worker.getBirthDate())
                .gender(worker.getGender())
                .address(worker.getAddress())
                .emergencyNumber(worker.getEmergencyNumber())
                .payReceive(worker.getPayReceive())
                .contractStartDate(worker.getContractStartDate())
                .contractEndDate(worker.getContractEndDate())
                // 은행 정보
                .bankName(bankName)
                .accountNumber(accountNum)
                .accountHolder(holder)
                .build();
    }
}
