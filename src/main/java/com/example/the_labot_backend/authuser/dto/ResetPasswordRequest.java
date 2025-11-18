package com.example.the_labot_backend.authuser.dto;

import lombok.Getter;

@Getter
public class ResetPasswordRequest {
    private String name;
    private String phoneNumber;
}