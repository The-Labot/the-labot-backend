package com.example.the_labot_backend.admins.dto;


import com.example.the_labot_backend.users.entity.Role;
import lombok.Getter;
import lombok.Setter;

// 임시 회원가입 dto
@Getter
@Setter
public class ManagerCreateRequest {
    private String phoneNumber;
    private String name;
}
