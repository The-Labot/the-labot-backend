package com.example.the_labot_backend.authUser.controller;

import com.example.the_labot_backend.authUser.service.AuthService;
import com.example.the_labot_backend.authUser.dto.AdminSignupRequest;
import com.example.the_labot_backend.authUser.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 로그인, 토큰값 반환
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok()
                .body(authService.login(request));
    }

    // 본사관리자 회원가입
    @PostMapping("/signup/admin")
    public ResponseEntity<?> signupAdmin(@RequestBody AdminSignupRequest request) {
        authService.signupAdmin(request);
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "본사관리자 회원가입 성공"
        ));
    }
}