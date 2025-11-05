package com.example.the_labot_backend.users.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginRequest {
    private String phoneNumber;
    private String password;
}
