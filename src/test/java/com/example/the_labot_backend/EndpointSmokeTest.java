package com.example.the_labot_backend;

import com.example.the_labot_backend.global.config.JwtTokenProvider;
import com.example.the_labot_backend.support.IntegrationTestSupport;
import com.example.the_labot_backend.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 주요 조회 엔드포인트가 예외 없이 응답하는지 확인한다.
 *
 * 응답 본문의 값은 검증하지 않는다. 이 테스트의 목적은 비즈니스 로직 검증이 아니라
 * 설정·구조 변경(open-in-view, 인덱스, 파일 업로드 구조, fetch 전략)으로 인해
 * 컴파일은 되지만 실행 시점에 실패하는 회귀를 잡는 것이다.
 *
 * 대상 선정 기준
 *   - file 테이블 조회(FileService.getFilesResponseByTarget)를 타는 엔드포인트.
 *     후속 작업인 인덱스 추가와 업로드 구조 변경이 이 경로를 직접 바꾼다
 *   - 다중 엔티티를 순회하는 대시보드. 지연 로딩 경로가 길어 설정 변경에 민감하다
 *
 * 인증은 실제 토큰을 발급해 헤더로 보낸다. JwtAuthenticationFilter가 토큰 없는 요청을
 * 거부하므로 SecurityContext를 직접 채우는 방식은 쓸 수 없고, 이 편이 필터와
 * 토큰 검증 경로까지 실제로 지나간다.
 */
class EndpointSmokeTest extends IntegrationTestSupport {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    TestDataFactory data;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        data.prepareOnce();
    }

    // ── 근로자 ───────────────────────────────────────────────

    /**
     * User → Worker → 파일 3종(계약서/급여명세서/자격증)으로 이어지는 조회.
     * 지연 로딩 경로가 가장 깊어 open-in-view 변경에 가장 민감하다.
     */
    @Test
    @DisplayName("근로자 마이페이지 조회")
    void workerMyPage() throws Exception {
        mockMvc.perform(get("/api/worker/mypage").header("Authorization", workerToken()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("근로자 공지 목록 조회")
    void workerNoticeList() throws Exception {
        mockMvc.perform(get("/api/worker/notices").header("Authorization", workerToken()))
                .andExpect(status().isOk());
    }

    // ── 현장관리자 ────────────────────────────────────────────

    /**
     * 여러 도메인(근로자 수, 출근 현황, 위험요소)을 순회하며 집계한다.
     */
    @Test
    @DisplayName("현장관리자 대시보드 조회")
    void managerDashboard() throws Exception {
        mockMvc.perform(get("/api/manager/dashboard").header("Authorization", managerToken()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("현장관리자 공지 목록/상세 조회")
    void managerNotices() throws Exception {
        mockMvc.perform(get("/api/manager/notices").header("Authorization", managerToken()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/manager/notices/" + data.getNoticeId()).header("Authorization", managerToken()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("현장관리자 위험요소 목록/상세 조회")
    void managerHazards() throws Exception {
        mockMvc.perform(get("/api/manager/hazards").header("Authorization", managerToken()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/manager/hazards/" + data.getHazardId()).header("Authorization", managerToken()))
                .andExpect(status().isOk());
    }

    /**
     * 후속 작업(N+1 개선)의 대상 엔드포인트.
     */
    @Test
    @DisplayName("현장관리자 작업일보 목록/상세 조회")
    void managerReports() throws Exception {
        mockMvc.perform(get("/api/manager/reports").header("Authorization", managerToken()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/manager/reports/" + data.getReportId()).header("Authorization", managerToken()))
                .andExpect(status().isOk());
    }

    /**
     * 파일 3종(교육자료/사진/서명)을 함께 조회한다. 파일 업로드 구조 변경의 영향 지점.
     */
    @Test
    @DisplayName("현장관리자 안전교육 목록/상세 조회")
    void managerEducations() throws Exception {
        mockMvc.perform(get("/api/manager/educations").header("Authorization", managerToken()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/manager/educations/" + data.getEducationId()).header("Authorization", managerToken()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("현장관리자 근로자 목록 조회")
    void managerWorkerList() throws Exception {
        mockMvc.perform(get("/api/manager/workers").header("Authorization", managerToken()))
                .andExpect(status().isOk());
    }

    // ── 본사관리자 ────────────────────────────────────────────

    @Test
    @DisplayName("본사관리자 현장별 대시보드 조회")
    void adminDashboard() throws Exception {
        mockMvc.perform(get("/api/admin/sites/" + data.getSiteId() + "/dashboard").header("Authorization", adminToken()))
                .andExpect(status().isOk());
    }

    // ── 인증 헬퍼 ─────────────────────────────────────────────
    //
    // JwtAuthenticationFilter가 토큰 없는 요청을 거부하므로, SecurityContext를 직접
    // 채우는 대신 운영과 동일하게 실제 토큰을 발급해 헤더로 보낸다.
    // 필터·토큰 검증·UserDetails 조회까지 실제 경로를 그대로 지나간다.

    private String bearer(Long userId, String role) {
        return "Bearer " + jwtTokenProvider.generateToken(userId, role);
    }

    private String workerToken() {
        return bearer(data.getWorkerId(), "ROLE_WORKER");
    }

    private String managerToken() {
        return bearer(data.getManagerId(), "ROLE_MANAGER");
    }

    private String adminToken() {
        return bearer(data.getAdminId(), "ROLE_ADMIN");
    }
}
