package com.example.the_labot_backend.workers.dto;

import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.files.dto.FileResponse;
import com.example.the_labot_backend.workers.entity.Worker;
import com.example.the_labot_backend.workers.entity.embeddable.WorkerBankAccount;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class WorkerMyPageResponse {
    // --- 1. 기본 인적 사항 ---
    private String name;            // [User] 이름
    private String phone;           // [User] 전화번호

    private String jobRole;         // [Worker] 직종 (position)
    private String siteName;        // [Worker] 상세 근무 현장명
    private String address;         // [Worker] 주소
    private LocalDate birthDate;    // [Worker] 생년월일
    private String gender;          // [Worker] 성별
    private String nationality;     // [Worker] 국적 (추가됨)
    private String profileImageUrl; // [Worker] 프로필 이미지 경로

    // --- 2. 계좌 정보 (WorkerBankAccount) ---
    private String bankName;
    private String accountNumber;
    private String accountHolder;

    // --- 3. [★ 수정됨] 파일 정보 (ID만 주는게 아니라 URL 포함된 객체 전달) ---
    private FileResponse contractFile;        // 근로계약서 (보통 1개)
    private List<FileResponse> payrollFiles;  // 급여명세서 (여러 달치, 리스트)
    private List<FileResponse> certificateFiles; // 자격증 (여러 개, 리스트)
    // --- 엔티티 -> DTO 변환 메서드 ---
    public static WorkerMyPageResponse from(User user, Worker worker,
                                            FileResponse contractFile,
                                            List<FileResponse> payrollFiles,
                                            List<FileResponse> certificateFiles) {

        // 계좌 정보 null 체크 (Worker가 계좌를 등록하지 않았을 수도 있음)
        WorkerBankAccount bank = worker.getBankAccount();
        String bName = (bank != null) ? bank.getBankName() : null;
        String bNum = (bank != null) ? bank.getAccountNumber() : null;
        String bHolder = (bank != null) ? bank.getAccountHolder() : null;

        return WorkerMyPageResponse.builder()
                // [User 엔티티에서 가져오는 정보]
                .name(user.getName())
                .phone(user.getPhoneNumber())

                // [Worker 엔티티에서 가져오는 정보]
                .jobRole(worker.getPosition())       // position -> jobRole 매핑
                .siteName(worker.getSiteName())      // 상세 현장명
                .address(worker.getAddress())        // 주소
                .birthDate(worker.getBirthDate())    // 생년월일
                .gender(worker.getGender())          // 성별
                .nationality(worker.getNationality())// 국적
                .profileImageUrl(worker.getProfileImage()) // 프로필 사진

                // [계좌 정보]
                .bankName(bName)
                .accountNumber(bNum)
                .accountHolder(bHolder)

                // [★] 파일 객체 매핑
                .contractFile(contractFile)
                .payrollFiles(payrollFiles)
                .certificateFiles(certificateFiles)
                .build();
    }
}
