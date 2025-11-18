package com.example.the_labot_backend.authUser.dto;

import lombok.Getter;

@Getter
public class ResetPasswordRequest {
    private String name;
    private String phoneNumber;
}