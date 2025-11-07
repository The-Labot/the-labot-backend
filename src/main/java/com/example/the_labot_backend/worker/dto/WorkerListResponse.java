package com.example.the_labot_backend.worker.dto;

import com.example.the_labot_backend.enums.WorkerStatus;
import lombok.*;

// 근로자 목록 조회 DTO
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WorkerListResponse {
    private Long id;
    private String name;
    private String profileImage;
    private String position;
    private WorkerStatus status;
}
