package com.example.the_labot_backend.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @PostMapping("/login")
    public String login() {
        return "로그인";
    }

    @PostMapping("/signup")
    public String signup() {
        return "회원가입";
    }
}
