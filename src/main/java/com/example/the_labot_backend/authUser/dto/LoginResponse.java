package com.example.the_labot_backend.authUser.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginResponse {
    private String token;
    private String role;
    private Long userId;
    private String name;
}
