package com.example.the_labot_backend.authUser.controller;

import com.example.the_labot_backend.authUser.dto.AdminSignupRequest;
import com.example.the_labot_backend.authUser.dto.LoginRequest;
import com.example.the_labot_backend.authUser.dto.ResetPasswordRequest;
import com.example.the_labot_backend.authUser.service.AuthService;
import com.example.the_labot_backend.authUser.service.UserService;
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
    private final UserService userService;

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

    // SMS 통해 임시 비밀번호 발급
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {

        userService.resetPassword(request.getPhoneNumber(),request.getName());

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "임시 비밀번호가 문자로 발송되었습니다."
        ));
    }
}