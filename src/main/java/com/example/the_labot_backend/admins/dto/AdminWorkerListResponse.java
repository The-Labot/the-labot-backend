package com.example.the_labot_backend.admins.dto;

import com.example.the_labot_backend.workers.entity.Worker;
import com.example.the_labot_backend.workers.entity.WorkerStatus;
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

    public static AdminWorkerListResponse from(Worker worker) {
        return AdminWorkerListResponse.builder()
                .workerId(worker.getId())
                .name(worker.getUser().getName())


                .siteName(worker.getUser().getSite().getProjectName()) //site에서 projectName가지고옴
                //.siteName(worker.getSiteName()) 아직 worker엔티티에 현장명이 존재하긴함.
                .position(worker.getPosition())
                .status(worker.getStatus())
                .phone(worker.getUser().getPhoneNumber())
                .contractType(worker.getContractType())
                .wageStartDate(worker.getWageStartDate())
                .wageEndDate(worker.getWageEndDate())
                .salary(worker.getSalary())
                .build();
    }
}
