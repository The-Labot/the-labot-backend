package com.example.the_labot_backend.authuser.dto;

import com.example.the_labot_backend.authuser.entity.Role;
import lombok.Getter;
import lombok.Setter;

// 임시 회원가입 dto
@Getter
@Setter
public class SignupRequest {
    private String phoneNumber;
    private String password;
    private String name;
    private Long siteId;
    private Role role; // ex) ROLE_WORKER, ROLE_ADMIN
}
