package com.example.the_labot_backend.authUser.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminSignupRequest {
    private String phoneNumber;
    private String password;
    private String name;
    private String email; // Admin 정보
    private String address; // Admin 정보
    private String secretCode; // 본사 비밀 코드
}
