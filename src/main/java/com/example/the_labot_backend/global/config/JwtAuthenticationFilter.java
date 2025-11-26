package com.example.the_labot_backend.global.config;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

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

        // 디버깅 용
        System.out.println("JwtFilter 실행됨: " + request.getRequestURI() + "\n");

        // 1) 인증 필요 없는 경로는 필터 통과
        String path = request.getServletPath();
        if (path.startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        //토큰 추출
        String token = resolveToken(request);

        if (token == null) {
            throw new IllegalArgumentException("JWT 토큰이 존재하지 않습니다.");
        }

        try {
            if (!jwtTokenProvider.validateToken(token)) {
                throw new MalformedJwtException("유효하지 않은 토큰입니다.");
            }

            String id = jwtTokenProvider.getIdFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            System.out.println(role);

            UserDetails userDetails = userDetailsService.loadUserByUsername(id);

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, List.of(new SimpleGrantedAuthority(role)));

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (ExpiredJwtException e) {
            throw e;
        } catch (SignatureException | MalformedJwtException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw e;
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
