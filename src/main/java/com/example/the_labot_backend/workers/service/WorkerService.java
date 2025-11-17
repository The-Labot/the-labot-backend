package com.example.the_labot_backend.workers.service;

import com.example.the_labot_backend.attendance.dto.AttendanceUpdateRequestDto;
import com.example.the_labot_backend.attendance.entity.Attendance;
import com.example.the_labot_backend.attendance.repository.AttendanceRepository;
import com.example.the_labot_backend.authUser.entity.Role;
import com.example.the_labot_backend.authUser.entity.User;
import com.example.the_labot_backend.authUser.repository.UserRepository;
import com.example.the_labot_backend.sites.entity.Site;
import com.example.the_labot_backend.workers.dto.WorkerCreateRequest;
import com.example.the_labot_backend.workers.dto.WorkerDetailResponse;
import com.example.the_labot_backend.workers.dto.WorkerListResponse;
import com.example.the_labot_backend.workers.dto.WorkerUpdateRequest;
import com.example.the_labot_backend.workers.entity.Worker;
import com.example.the_labot_backend.workers.repository.WorkerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkerService {

    private final WorkerRepository workerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AttendanceRepository attendanceRepository;// 워크서비스에서 attendace클래스를 변경가능하게끔 함.  11/16박찬홍

    // 근로자 등록
    @Transactional
    public void createWorker(Long managerId, WorkerCreateRequest request) {

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.(getReportsByUser) userId:" + managerId));

        Site site = manager.getSite();

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
                .build();

        userRepository.save(workerUser);

        // Worker 상세정보 생성
        Worker worker = Worker.builder()
                .user(workerUser)
                .build();

        workerRepository.save(worker);
    }

    // 근로자 목록 조회
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

    // 근로자 상세 조회
    // 추후 출퇴근기록 등 기능 추가
    public WorkerDetailResponse getWorkerDetail(Long workerId) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("해당 근로자를 찾을 수 없습니다."));

        return WorkerDetailResponse.builder()
                .id(worker.getId())
                .name(worker.getUser().getName())
                .phone(worker.getUser().getPhoneNumber())
                .address(worker.getAddress())
                .birthDate(worker.getBirthDate())
                .gender(worker.getGender())
                .nationality(worker.getNationality())
                .position(worker.getPosition())
                .site(worker.getUser().getSite())
                .build();
    }

    // 근로자 정보 수정 
    public void updateWorker(Long id, WorkerUpdateRequest dto) {
        Worker worker = workerRepository.findById(id)
                // 해당하는 id가 없을경우 예외를 던짐
                .orElseThrow(() -> new EntityNotFoundException("근로자를 찾을 수 없습니다."));
        worker.setAddress(dto.getAddress());
        worker.setPosition(dto.getPosition());
        worker.setStatus(dto.getStatus());
        workerRepository.save(worker);
    }

    // 박찬홍 11/16일 추가
    @Transactional
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

        // 4. DB에 최종 저장
        attendanceRepository.save(record);
    }
}
