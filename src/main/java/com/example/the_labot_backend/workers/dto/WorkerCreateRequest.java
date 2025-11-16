package com.example.the_labot_backend.workers.dto;

import lombok.Getter;
import lombok.Setter;

// 회원가입
// 추후에 추가
@Getter
@Setter
public class WorkerCreateRequest {
    private String phoneNumber;
    private String name;
}
