package com.example.the_labot_backend.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String phoneNumber;
    private String password;
    private String clientType; // "APP" 또는 "WEB"
}