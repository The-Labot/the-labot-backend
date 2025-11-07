package com.example.the_labot_backend.global.config;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // JWT를 암호화/복호화할 때 쓰는 비밀키
    // 유출되면 안 됨으로 데이터 숨겨야함.
    private final Key key;

    // 토큰 유지 기간
    private final long accessTokenValidity;

    // 환경변수에서 키와 유효기간을 모두 주입받음
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-validity}") long accessTokenValidity
    ) {
        // HMAC SHA256 서명용 Key 객체로 만들어줌.
        // key를 이용해 JWT의 서명과 검증 수행
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidity = accessTokenValidity;
    }



    // 토큰 생성
    public String generateToken(String phoneNumber, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenValidity);

        return Jwts.builder()
                .setSubject(phoneNumber) // JWT의 주체(식별자)
                .claim("role", role)  // ✅ 권한 정보 추가
                .setIssuedAt(now) // 발급시간
                .setExpiration(expiryDate) // 만료시간
                .signWith(key, SignatureAlgorithm.HS256) // 비밀키로 서명 생성
                .compact(); // 최종적으로 Base64 인코딩된 문자열 형태로 반환
    }

    // 토큰에서 전화번호 추출
    public String getPhoneNumberFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 토큰에서 Role 추출
    public String getRoleFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }


    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}

