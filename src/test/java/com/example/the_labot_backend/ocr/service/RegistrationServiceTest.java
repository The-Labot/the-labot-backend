package com.example.the_labot_backend.ocr.service;

import com.example.the_labot_backend.enums.WorkerStatus;
import com.example.the_labot_backend.ocr.dto.FinalSaveDto;
import com.example.the_labot_backend.users.User;
import com.example.the_labot_backend.users.UserRepository;
import com.example.the_labot_backend.workers.Worker;
import com.example.the_labot_backend.workers.WorkerRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
public class RegistrationServiceTest {

    @Autowired
    RegistrationService registrationService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    WorkerRepository workerRepository;

    @Autowired
    EntityManager em; // (3) DB 동기화를 위한 도구

    @BeforeEach
    void setup() {

        // DB를 깨끗하게 청소해서 중복 오류를 방지
        // (주의: User를 참조하는 Worker를 먼저 지워야 함)
        workerRepository.deleteAll();
        userRepository.deleteAll();
    }
    // ▲▲▲▲▲ [2. 이 메서드 추가] ▲▲▲▲▲

    @Test
    @DisplayName("여성_근로자_및_추가_필드_저장_테스트")
    void saveAndFindFemaleWorkerTest() {

        // 1. Given (준비): "이영희" (여성) 정보 및 추가 필드(국적, 현장명)
        FinalSaveDto dto = new FinalSaveDto();
        dto.setName("이영희");
        dto.setAddress("부산광역시 해운대구 우동 1234");
        dto.setResidentIdNumber("980202-2112233"); // "여성", 1998-02-02
        dto.setPhoneNumber("010-1111-2222"); // (DB 중복 방지용 새 번호)
        dto.setJobType("용접공");
        dto.setNationality("대한민국"); // ★ 추가된 필드
        dto.setSiteName("해운대 신도시 A블럭"); // ★ 추가된 필드

        // (급여, 계약기간은 필수 필드가 아니므로 테스트에서 생략 가능)

        // 2. When (실행): 저장
        User savedUser = registrationService.saveNewMember(dto);

        // DB 즉시 반영
        em.flush();
        em.clear();

        // 3. Then (검증): DB에서 다시 찾아오기
        Worker foundWorker = workerRepository.findById(savedUser.getId())
                .orElseThrow(() -> new AssertionError("Worker가 DB에 저장되지 않았습니다."));

        // [★] "저장한 사람과 찾아온 사람이 같은지" 검증

        // (1) DTO에서 직접 받은 값 검증
        assertThat(foundWorker.getUser().getName()).isEqualTo("이영희");
        assertThat(foundWorker.getAddress()).isEqualTo("부산광역시 해운대구 우동 1234");
        assertThat(foundWorker.getPosition()).isEqualTo("용접공");

        // (2) 우리가 방금 추가한 'null이 아니어야 할' 필드 검증
        assertThat(foundWorker.getNationality()).isEqualTo("대한민국");
        assertThat(foundWorker.getSiteName()).isEqualTo("해운대 신도시 A블럭");
        assertThat(foundWorker.getStatus()).isEqualTo(WorkerStatus.WAITING); // (서비스가 기본값으로 저장)

        // (3) 주민번호에서 파생된 로직 검증
        assertThat(foundWorker.getGender()).isEqualTo("여성"); // ★ 성별 로직 검증
        assertThat(foundWorker.getBirthDate()).isEqualTo(LocalDate.of(1998, 2, 2)); // ★ 생년월일 로직 검증
    }


}