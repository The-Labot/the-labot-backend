package com.example.the_labot_backend.workers.dto;

import com.example.the_labot_backend.attendance.entity.AttendanceStatus;
import com.example.the_labot_backend.files.dto.FileResponse;
import com.example.the_labot_backend.sites.entity.Site;
import com.example.the_labot_backend.workers.entity.WorkerStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// 근로자 세부 조회 응답 DTO
@Getter
@Builder
public class WorkerDetailResponse {
    private Long id;
    private String name;
    private String phone;
    private String address;
    private LocalDate birthDate;
    private String gender;
    private String nationality;
    private String position;
    private String siteProjectName;

    private WorkerStatus status; // 근무중, 대기중 등

    // [2] 계약 및 급여 정보 (등록 시 입력값)
    private String contractType;
    private String salary;
    private String payReceive;
    private LocalDate wageStartDate;
    private LocalDate wageEndDate;
    private String emergencyNumber; // 비상 연락처

    // [3] 금융 정보 (이게 있어야 .bankName()이 동작함)
    private String bankName;
    private String accountNumber;
    private String accountHolder;

    // [4] 출퇴근 및 이의제기 기록 리스트
    private List<AttendanceLogDto> attendanceHistory;
    // 1. 근로계약서 (보통 1개만 있으므로 단일 객체, 없으면 null)
    private FileResponse contractFile;

    // 2. 임금명세서 (여러 달치일 수 있으므로 리스트)
    private List<FileResponse> payStubFiles;

    // 3. 자격증 (여러 개일 수 있으므로 리스트)
    private List<FileResponse> licenseFiles;

    @Getter
    @Builder
    public static class AttendanceLogDto {
        private Long attendanceId;
        private LocalDate date;
        private LocalTime clockInTime;
        private LocalTime clockOutTime;
        private AttendanceStatus status;
        private String objectionMessage; // 이의제기 내용
    }
}
