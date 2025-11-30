package com.example.the_labot_backend.workers.service;

import com.example.the_labot_backend.attendance.dto.AttendanceUpdateRequestDto;
import com.example.the_labot_backend.attendance.entity.Attendance;
import com.example.the_labot_backend.attendance.repository.AttendanceRepository;
import com.example.the_labot_backend.authuser.entity.Role;
import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.files.dto.FileResponse;
import com.example.the_labot_backend.files.service.FileService;
import com.example.the_labot_backend.headoffice.entity.HeadOffice;
import com.example.the_labot_backend.ocr.dto.FinalSaveDto;
import com.example.the_labot_backend.sites.entity.Site;

import com.example.the_labot_backend.workers.dto.WorkerDashboardResponse;
import com.example.the_labot_backend.workers.dto.WorkerDetailResponse;
import com.example.the_labot_backend.workers.dto.WorkerListResponse;
import com.example.the_labot_backend.workers.dto.WorkerUpdateRequest;
import com.example.the_labot_backend.workers.entity.Worker;
import com.example.the_labot_backend.workers.entity.WorkerStatus;
import com.example.the_labot_backend.workers.entity.embeddable.WorkerBankAccount;
import com.example.the_labot_backend.workers.repository.WorkerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AttendanceRepository attendanceRepository;// 워크서비스에서 attendace클래스를 변경가능하게끔 함.  11/16박찬홍
    private final FileService fileService;

    // 근로자 등록
    @Transactional
    public void createWorker(Long managerId, FinalSaveDto request) {

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.(getReportsByUser) userId:" + managerId));

        Site site = manager.getSite();
        HeadOffice headOffice = site.getHeadOffice();

        if (manager.getRole() != Role.ROLE_MANAGER) {
            throw new RuntimeException("현장관리자만 근로자를 등록할 수 있습니다.");
        }

        // 전화번호 중복 체크
        if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new RuntimeException("이미 존재하는 전화번호입니다.");
        }

        // 임시 비밀번호 생성
        String tempPw = "1234"; // 또는 랜덤 생성 가능

        // User 생성 (ROLE_WORKER)
        User workerUser = User.builder()
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(tempPw))
                .name(request.getName())
                .role(Role.ROLE_WORKER)
                .site(site)
                .headOffice(headOffice) // [★ 추가] 본사 정보 직접 주입!
                .build();

        userRepository.save(workerUser);


        // 4. 주민번호로 성별/생년월일 추출
        String rrn = request.getResidentIdNumber();
        String gender = determineGender(rrn);
        LocalDate birthDate = determineBirthDate(rrn);

        // 5. BankAccount (계좌 정보) 생성
        WorkerBankAccount bankAccount = WorkerBankAccount.builder()
                .bankName(request.getBankName())
                .accountNumber(request.getAccountNumber())
                .accountHolder(request.getAccountHolder())
                .build();

        // 6. Worker 생성 (OCR 상세 정보 매핑)
        Worker worker = Worker.builder()
                .user(workerUser)
                .address(request.getAddress())
                .gender(gender)
                .birthDate(birthDate)
                .nationality(request.getNationality())
                .position(request.getJobType()) // 직종
                .siteName(request.getSiteName()) // 텍스트로도 저장

                // [★ 계약서 정보]
                .contractType(request.getContractType())
                .salary(request.getSalary())
                .emergencyNumber(request.getEmergencyNumber()) // [★] 비상연락처 저장
                .payReceive(request.getPayReceive())
                // [★ 추가됨] 계약 시작일/종료일 매핑
                .contractStartDate(request.getContractStartDate())
                .contractEndDate(request.getContractEndDate())
                .wageStartDate(request.getWageStartDate())
                .wageEndDate(request.getWageEndDate())
                .bankAccount(bankAccount) // 계좌 정보 내장

                .status(WorkerStatus.WAITING) // 기본값 대기중
                .build();

        workerRepository.save(worker);
    }
    // --- 헬퍼 메서드 ---
    private String determineGender(String rrn) {
        if (rrn == null || rrn.length() < 8) return null;
        char genderDigit = rrn.charAt(7);
        return (genderDigit == '1' || genderDigit == '3') ? "남성" : "여성";
    }

    private LocalDate determineBirthDate(String rrn) {
        if (rrn == null || rrn.length() < 8) return null;
        try {
            String birthStr = rrn.substring(0, 6);
            char genderDigit = rrn.charAt(7);
            String prefix = (genderDigit == '3' || genderDigit == '4') ? "20" : "19";
            return LocalDate.parse(prefix + birthStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) { return null; }
    }

    // 근로자 목록 조회
    @Transactional(readOnly = true)
    public List<WorkerListResponse> getWorkers() {
        List<Worker> workers = workerRepository.findAll();

        // stream API
        // 리스트를 흐름으로 바꿔서 데이터를 반복문처럼 다루되, 선언형으로 쓸 수 있게 해줌
        return workers.stream()
                // map()은 리스트의 각 원소를 다른 형태로 바꿔주는 함수
                // worker를 WorkerListResponse로 변환하는 작업
                // 빌더 패턴
                // 객체를 만드는 방법을 편하게
                // 인자가 많을 때 가독성 좋게 쓸 수 있음
                // 마지막에 .build()를 호출하면 객체가 완성됨.
                .map(worker -> WorkerListResponse.builder()
                        .id(worker.getId())
                        .name(worker.getUser().getName())
                        .profileImage(worker.getProfileImage())
                        .position(worker.getPosition())
                        .status(worker.getStatus())
                        .build())
                // 스트림을 다시 리스트로 변환
                .toList();
    }
    // [★ 신규] 대시보드용 근로자 목록 및 통계 조회 (퇴직자 제외)
    @Transactional(readOnly = true)
    public WorkerDashboardResponse getWorkerDashboard(Long managerId) {

        // 1. 관리자 정보로 현장 ID 찾기
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("관리자 정보 없음"));
        Long siteId = manager.getSite().getId();

        // 2. 퇴직자(RETIRED)를 제외한 현장 근로자 전체 조회
        List<Worker> workers = workerRepository.findByUser_Site_IdAndStatusNot(siteId, WorkerStatus.RETIRED);

        // 3. 이의제기가 있는 근로자 ID 목록 조회 -> Set으로 변환 (검색 속도 향상)
        List<Long> objectionIds = attendanceRepository.findWorkerIdsWithObjection(siteId);
        Set<Long> objectionSet = new HashSet<>(objectionIds);

        // 4. 통계 계산
        long activeCount = workers.stream().filter(w -> w.getStatus() == WorkerStatus.ACTIVE).count();
        long waitingCount = workers.stream().filter(w -> w.getStatus() == WorkerStatus.WAITING).count();
        long totalCount = workers.size(); // Active + Waiting (퇴직자 제외됨)

        // 이의제기 수 계산 (현재 목록에 있는 사람 중에서만 카운트)
        long objectionCount = workers.stream()
                .filter(w -> objectionSet.contains(w.getId()))
                .count();

        // 5. DTO 변환 (WorkerListResponse 재사용)
        List<WorkerListResponse> dtoList = workers.stream()
                .map(w -> WorkerListResponse.builder()
                        .id(w.getId())
                        .name(w.getUser().getName())
                        .profileImage(w.getProfileImage())
                        .position(w.getPosition())
                        .status(w.getStatus())
                        // ★ 이의제기 여부 체크 (Set에 ID가 있으면 true)
                        .hasObjection(objectionSet.contains(w.getId()))
                        .build())
                .toList();

        // 6. 최종 응답 객체 빌드
        return WorkerDashboardResponse.builder()
                .totalCount(totalCount)
                .activeCount(activeCount)
                .waitingCount(waitingCount)
                .objectionCount(objectionCount)
                .workers(dtoList)
                .build();
    }

    // 근로자 상세 조회
    // 추후 출퇴근기록 등 기능 추가
    @Transactional(readOnly = true)
    public WorkerDetailResponse getWorkerDetail(Long workerId) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("해당 근로자를 찾을 수 없습니다."));
        // 1. 근로계약서 (우리가 저장할 때 targetType="WORKER_CONTRACT"로 하기로 약속)
        List<FileResponse> contractList = fileService.getFilesResponseByTarget("WORKER_CONTRACT", workerId);
        FileResponse contractFile = contractList.isEmpty() ? null : contractList.get(0); // 1개만 꺼냄

        // 2. 임금명세서 (targetType="WORKER_PAYSTUB" - 나중에 급여대장 만들 때 이 타입으로 저장하면 됨)
        List<FileResponse> payStubs = fileService.getFilesResponseByTarget("WORKER_PAYSTUB", workerId);

        // 3. 자격증 (targetType="WORKER_LICENSE" - 자격증 올릴 때 이 타입으로 저장)
        List<FileResponse> licenses = fileService.getFilesResponseByTarget("WORKER_LICENSE", workerId);

        // 2. [추가] 출퇴근 기록(List<Attendance>) -> DTO 리스트로 변환
        List<WorkerDetailResponse.AttendanceLogDto> attendanceLogs = worker.getAttendanceRecords().stream()
                .map(record -> WorkerDetailResponse.AttendanceLogDto.builder()
                        .attendanceId(record.getId())
                        .date(record.getDate())
                        .clockInTime(record.getClockInTime())
                        .clockOutTime(record.getClockOutTime())
                        .status(record.getStatus())
                        .objectionMessage(record.getObjectionMessage()) // 이의제기
                        .build())
                .toList();

        // 3. [추가] 계좌 정보 null 체크
        String bankName = null;
        String accountNum = null;
        String accountHolder = null;

        if (worker.getBankAccount() != null) {
            bankName = worker.getBankAccount().getBankName();
            accountNum = worker.getBankAccount().getAccountNumber();
            accountHolder = worker.getBankAccount().getAccountHolder();
        }

        return WorkerDetailResponse.builder()

                .id(worker.getId())
                .name(worker.getUser().getName())
                .phone(worker.getUser().getPhoneNumber())
                .address(worker.getAddress())
                .birthDate(worker.getBirthDate())
                .gender(worker.getGender())
                .nationality(worker.getNationality())
                .position(worker.getPosition())
                .siteName(worker.getSiteName())
                // --- [추가 1] 상태/프로필 ---
                .status(worker.getStatus())

                // --- [추가 2] 계약 정보 ---
                .contractType(worker.getContractType())
                .salary(worker.getSalary())
                .payReceive(worker.getPayReceive())
                // [★ 추가됨] 계약 시작일/종료일 매핑
                .contractStartDate(worker.getContractStartDate())
                .contractEndDate(worker.getContractEndDate())
                .wageStartDate(worker.getWageStartDate())
                .wageEndDate(worker.getWageEndDate())
                .emergencyNumber(worker.getEmergencyNumber())

                // --- [추가 3] 금융 정보 ---
                .bankName(bankName)
                .accountNumber(accountNum)
                .accountHolder(accountHolder)
                .contractFile(contractFile)
                .payStubFiles(payStubs)
                .licenseFiles(licenses)

                // --- [추가 4] 출퇴근 기록 ---
                .attendanceHistory(attendanceLogs)
                .build();
    }

    // 근로자 정보 수정
    @Transactional
    public void updateWorker(Long id, WorkerUpdateRequest dto) {
        Worker worker = workerRepository.findById(id)
                // 해당하는 id가 없을경우 예외를 던짐
                .orElseThrow(() -> new EntityNotFoundException("근로자를 찾을 수 없습니다."));
        if (dto.getSiteName() != null) {
            worker.setSiteName(dto.getSiteName());
        }
        if (dto.getPosition() != null) {
            worker.setPosition(dto.getPosition());
        }
        if (dto.getStatus() != null) {
            worker.setStatus(dto.getStatus());
        }
        //workerRepository.save(worker);
    }

    // 박찬홍 11/16일 추가
    @Transactional //이의제기 처리 및 시간 변경
    public void updateAttendanceRecord(Long attendanceId, AttendanceUpdateRequestDto dto) {

        // 1. ID로 수정할 출퇴근 기록을 찾음
        Attendance record = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new EntityNotFoundException("해당 출퇴근 기록을 찾을 수 없습니다. ID: " + attendanceId));

        // 2. DTO에 값이 있으면, 그 값으로 덮어씀
        if (dto.getClockInTime() != null) {
            record.setClockInTime(dto.getClockInTime());
        }
        if (dto.getClockOutTime() != null) {
            record.setClockOutTime(dto.getClockOutTime());
        }
        if (dto.getStatus() != null) {
            record.setStatus(dto.getStatus());
        }

        // 3. [★네 요청★] 이의제기를 확인했으니, 이의제기 메시지 필드를 null로 변경
        record.setObjectionMessage(null);
        // 1) 상태값이 변경된 경우 처리
        Worker worker = record.getWorker(); // 연관된 근로자 가져오기
        if (dto.getStatus() != null) {
            switch (dto.getStatus()) {
                case PRESENT:      // 정상 출근 -> 근무중
                case LATE:         // 지각 -> 근무중
                    worker.setStatus(WorkerStatus.ACTIVE);
                    break;
                case EARLY_LEAVE:  // 조퇴 -> 대기중(퇴근)
                case ABSENT:       // 결석 -> 대기중
                    worker.setStatus(WorkerStatus.WAITING);
                    break;
            }
        }

        // 2) [방어 로직] 관리자가 '퇴근 시간'을 입력했다면 무조건 대기중(퇴근) 처리
        // (상태가 PRESENT여도 퇴근 시간이 찍히면 일 끝난 거니까요)
        if (dto.getClockOutTime() != null) {
            worker.setStatus(WorkerStatus.WAITING);
        }

        // 4. DB에 최종 저장
        attendanceRepository.save(record);
    }
}
