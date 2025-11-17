package com.example.the_labot_backend.ocr.service;

import com.example.the_labot_backend.ocr.dto.FinalSaveDto;
import com.example.the_labot_backend.users.UserRepository;
import com.example.the_labot_backend.users.entity.Role;
import com.example.the_labot_backend.users.entity.User;
import com.example.the_labot_backend.workers.Worker;
import com.example.the_labot_backend.workers.WorkerRepository;
import com.example.the_labot_backend.workers.WorkerStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final UserRepository userRepository;
    private final WorkerRepository workerRepository;

    /**
     * [★ 대규모 수정 ★]
     * OCR DTO(FinalSaveDto)를 받아서 'User'와 'Worker' 엔티티 2개를 생성하고 저장
     */
    @Transactional // 2개의 DB 저장을 하나로 묶음
    public User saveNewMember(FinalSaveDto dto) {

        // --- 1단계: 'User' (부모) 생성 및 저장 ---
        // (주의: password, role 등은 DTO에 없으므로, 기본값을 설정해야 함)
        User newUser = User.builder()
                .phoneNumber(dto.getPhoneNumber())
                .name(dto.getName())
                .password("임시비밀번호1234") // [!] 친구와 상의 후 수정 필요 (암호화 등)
                .role(Role.ROLE_WORKER)      // [!] 기본값 (친구 Enum에 맞게)
                // .site(null) // 현장은 나중에 배정
                .build();

        // ★ User를 먼저 save() 해서 DB ID를 생성
        User savedUser = userRepository.save(newUser);

        // --- 2단계: 'Worker' (자식) 생성 및 저장 ---

        String rrn = dto.getResidentIdNumber();
        String gender = determineGender(rrn); // 성별 판별 로직
        LocalDate birthDate = determineBirthDate(rrn); // 생년월일 추출 로직

        Worker newWorker = Worker.builder()
                .user(savedUser) // [★] 1단계에서 저장한 User를 여기에 연결
                .address(dto.getAddress())
                .gender(gender)
                .birthDate(birthDate)
                .position(dto.getJobType()) // DTO의 직종(jobType)을 position에 매핑
                .nationality(dto.getNationality()) // [★1] 국적 저장
                .siteName(dto.getSiteName())     // [★2] 현장명 저장
                .status(WorkerStatus.WAITING)  // [★3] 'status'는 "대기중"으로 기본값 설정
                .build();// .status(WorkerStatus.WAITING) // (기본값 설정은 친구와 상의)

        // ★ Worker를 save() 하면 @MapsId가 User의 ID를 Worker의 ID로 자동 설정
        workerRepository.save(newWorker);

        return savedUser; // 대표로 User 객체를 반환
    }

    /**
     * [★ 네가 만든 헬퍼 메서드 ★]
     * 주민등록번호(rrn)를 받아서 성별을 판별
     */
    private String determineGender(String rrn) {
        if (rrn == null || rrn.length() < 8 || rrn.charAt(6) != '-') {
            return null; // (DB에 "알 수 없음" 대신 null로 저장)
        }
        char genderDigit = rrn.charAt(7);
        switch (genderDigit) {
            case '1': case '3': return "남성";
            case '2': case '4': return "여성";
            default: return null;
        }
    }

    /**
     * [★ 새로 추가한 헬퍼 메서드 ★]
     * 주민등록번호(rrn)에서 생년월일(LocalDate)을 추출
     */
    private LocalDate determineBirthDate(String rrn) {
        if (rrn == null || rrn.length() < 8) {
            return null;
        }
        try {
            String birthStr = rrn.substring(0, 6); // "970103"
            char genderDigit = rrn.charAt(7);

            // 2000년대생 (3, 4)
            if (genderDigit == '3' || genderDigit == '4') {
                birthStr = "20" + birthStr;
            } else {
                // 1900년대생 (1, 2)
                birthStr = "19" + birthStr;
            }

            return LocalDate.parse(birthStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            return null; // 파싱 실패 시
        }
    }
    public List<Worker> getAllWorkers() {
        return workerRepository.findAll();
    }

}
