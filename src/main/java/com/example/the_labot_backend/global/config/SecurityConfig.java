package com.example.the_labot_backend.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true) // @PreAuthorize를 사용하기 위해 설정
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // csrf는 세션 기반 로그인에서 공격 방지를 위한 기능, 토큰 기반 로그인이므로 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 세션 생성 x
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 요청 URL 접근 권한

                .authorizeHttpRequests(auth -> auth
                        // auth관련 api 로그인, 회원가입 허용
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/user/**").hasAnyRole("WORKER", "MANAGER", "ADMIN")
                        // security에서는 사용자 권한이 ROLE_XXX 형태로 저장됨.
                        // hasRole("XXX") 하면 Role_XXX로 붙여 검사
                        // XXX의 권한을 가지고 있어야 접근 가능
                        .requestMatchers("/api/manager/**").hasRole("MANAGER")//현장관리자
                        .requestMatchers("/api/worker/**").hasRole("WORKER")//현장근로자
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")//현장근로자

                        // swagger 접속할 수 있게 해제
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/swagger-resources",
                                "/webjars/**"
                        ).permitAll()
                        .requestMatchers("/api/register/**").permitAll() //ocr이용을 위해11/13일 박찬홍 추가
                        .anyRequest().authenticated()
                )
                //요청이 들어오면 필터를 여러개 거쳐 검증
                //UsernamePasswordAuthenticationFilter는 로그인 요청을 처리하는 기본 필터
                //따로 커스텀으로 jwtAuthenticationFilter를 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:3000");  // 리액트 개발 서버
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    //AuthenticationManager은 인증 담당 객체
    //로그인 시 ID/PW를 검증할 때 authenticate() 호출
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        //단방향 해시 알고리즘으로 암호화
        return new BCryptPasswordEncoder();
    }

    //회원가입 시 passwordEncoder.encode(rawPassword)로 저장
    //로그인 시 matches(raw,encoded)로 비교
}
