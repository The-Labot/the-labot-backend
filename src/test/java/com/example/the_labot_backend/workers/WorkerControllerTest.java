package com.example.the_labot_backend.workers;

import com.example.the_labot_backend.global.config.SecurityConfig;
import com.example.the_labot_backend.global.config.JwtAuthenticationFilter;
import com.example.the_labot_backend.workers.dto.WorkerDetailResponse;
import com.example.the_labot_backend.workers.dto.WorkerListResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@SpringBootTest // 스프링 부트를 통째로 실행해서 테스트 환경을 구성, 빈을 전부 로드
//@AutoConfigureMockMvc // MockMvc 객체를 자동 설정, MockMvc는 가짜 HTTP 요청을 만들어서 Controller를 테스트할 수 있게 도와주는 도구
//@AutoConfigureMockMvc(addFilters = false) // 테스트할 때 보안 해제

//@WebMvcTest(controllers = WorkerController.class)
@WebMvcTest(
        controllers = WorkerController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        },
        // 💡 추가: 프로젝트의 Security 설정 파일 자체를 제외
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                    SecurityConfig.class,
                    JwtAuthenticationFilter.class
                }
        )
)
// @AutoConfigureMockMvc(addFilters = false) // security 관련 비활성화
class WorkerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkerService workerService;

    @Test
    @DisplayName("근로자 목록 조회 API (성공)")
    void getAllWorkersTest() throws Exception {

        // given
        List<WorkerListResponse> mockResponse = List.of(
                WorkerListResponse.builder()
                        .id(1L)
                        .name("홍길동")
                        .position("철근공")
                        .status(WorkerStatus.ACTIVE)
                        .build(),
                WorkerListResponse.builder()
                        .id(2L)
                        .name("김영희")
                        .position("타일공")
                        .status(WorkerStatus.WAITING)
                        .build()
        );

        given(workerService.getWorkers()).willReturn(mockResponse);

        // when & then

        // HTTP GET 요청을 전송
        mockMvc.perform(get("/api/manager/workers"))
                // 200 OK인지 확인, 메세지가 같은지 확인, data필드가 배열인지 확인 (JSON 구조)
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.message").value("근로자 목록 조회 성공"))
                .andExpect(jsonPath("$.data[0].name").value("홍길동"))
                .andExpect(jsonPath("$.data[1].status").value("WAITING"));
    }


    @Test
    @DisplayName("근로자 상세 조회 API 성공")
    void getWorkerDetail_Success() throws Exception {
        // given
        WorkerDetailResponse mockDetail = WorkerDetailResponse.builder()
                .id(1L)
                .name("홍길동")
                .phone("01012345678")
                .address("서울시 강남구")
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender("남성")
                .nationality("대한민국")
                .build();

        given(workerService.getWorkerDetail(1L)).willReturn(mockDetail);

        // when & then
        mockMvc.perform(get("/api/manager/workers/{workerId}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.phone").value("01012345678"))
                .andExpect(jsonPath("$.data.birthDate").value("1990-01-01"));
    }

    // jsonPath("$.data.id")는 JSON 응답의 특정 필드를 지정하는 XPath 같은 문법?

}
