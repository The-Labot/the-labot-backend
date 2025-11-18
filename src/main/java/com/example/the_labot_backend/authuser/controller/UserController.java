package com.example.the_labot_backend.authuser.controller;

import com.example.the_labot_backend.authuser.dto.ChangePasswordRequest;
import com.example.the_labot_backend.authuser.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(auth.getName());

        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "비밀번호가 성공적으로 변경되었습니다."
        ));
    }
}
