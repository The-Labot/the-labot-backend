package com.example.the_labot_backend.workers;


import com.example.the_labot_backend.users.entity.User;
import com.example.the_labot_backend.workers.dto.WorkerDetailResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

class WorkerServiceTest {

    // 실제 DB를 쓰지 않고 가짜로 대체
    @Mock
    private WorkerRepository workerRepository;

    // 자동으로 Repository를 Service에 주입시킴
    @InjectMocks
    private WorkerService workerService;

    private Worker worker;
    private User user;

    @BeforeEach
    void setUp() {
        // Mock 객체를 초기화하고 아래에 가짜 데이터를 만듬
        MockitoAnnotations.openMocks(this);

        user = User.builder()
                .id(1L)
                .name("홍길동")
                .phoneNumber("01012345678")
                .build();

        worker = Worker.builder()
                .id(1L)
                .user(user)
                .address("서울시 강남구")
                .birthDate(LocalDate.of(1995, 5, 10))
                .gender("남성")
                .nationality("대한민국")
                .build();
    }

    @Test
    @DisplayName("근로자 상세 조회 성공")
    void getWorkerDetail_Success() {
        
        // given: Mock 객체의 행동을 지정하는 영역
        given(workerRepository.findById(1L)).willReturn(Optional.of(worker));

        // when: 
        WorkerDetailResponse response = workerService.getWorkerDetail(1L);

        // then: 기대한 것과 일치하는지 검증
        assertThat(response.getId()).isEqualTo(worker.getId());
        assertThat(response.getName()).isEqualTo(worker.getUser().getName());
        assertThat(response.getPhone()).isEqualTo(worker.getUser().getPhoneNumber());
        assertThat(response.getAddress()).isEqualTo(worker.getAddress());
        assertThat(response.getBirthDate()).isEqualTo("1995-05-10");
    }

    @Test
    @DisplayName("존재하지 않는 근로자 조회 시 예외 발생")
    void getWorkerDetail_NotFound() {
        // given
        given(workerRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> workerService.getWorkerDetail(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("해당 근로자를 찾을 수 없습니다.");
    }
}
