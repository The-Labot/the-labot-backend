package com.example.the_labot_backend.workers.dto;

import com.example.the_labot_backend.workers.entity.WorkerStatus;
import lombok.Getter;
import lombok.Setter;

// 근로자 수정 요청 DTO
@Getter
@Setter
public class WorkerUpdateRequest {
    private String address;
    private String position;
    private WorkerStatus status;
}
