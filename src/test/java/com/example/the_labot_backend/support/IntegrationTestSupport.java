package com.example.the_labot_backend.support;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 통합 테스트 공통 설정.
 *
 * 운영과 같은 MySQL 8.0에 붙어 검증한다. H2를 쓰면 @Query의 MySQL 전용 문법
 * (FUNCTION('YEAR', ...) 등)을 해석하지 못해 통과의 의미가 약해진다.
 *
 * DB는 로컬에서는 docker-compose.local.yml의 컨테이너를, CI에서는 워크플로의
 * 서비스 컨테이너를 사용한다. 접속 정보는 application-test.yaml 참고.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {
}
