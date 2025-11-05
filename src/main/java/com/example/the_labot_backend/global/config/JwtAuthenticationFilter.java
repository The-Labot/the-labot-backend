package com.example.the_labot_backend.global.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
//OncePerRequestFilter는 요청마다 딱 한번 실행되는 필터
//요청이 1회 들어올 때 마다 doFilterInternal()이 호출됨.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    //JwtTokenProvider는 JWT를 생성,파싱,유효성 검증 헬퍼 클래스
    private final JwtTokenProvider jwtTokenProvider;
    //스프링 시큐리티가 제공하는 인터페이스, DB에서 사용자 정보를 가져오는 역할
    private final UserDetailsService userDetailsService;

    //DI
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("JwtFilter 실행됨: " + request.getRequestURI());

        //토큰 추출
        String token = resolveToken(request);
        //토큰 유효성 검증
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String phoneNumber = jwtTokenProvider.getPhoneNumberFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(phoneNumber);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
            //SecurityContext 등록
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
