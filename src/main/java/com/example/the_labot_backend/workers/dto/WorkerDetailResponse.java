package com.example.the_labot_backend.workers.dto;

import com.example.the_labot_backend.sites.entity.Site;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

// 근로자 세부 조회 응답 DTO
@Getter
@Builder
public class WorkerDetailResponse {
    private Long id;
    private String name;
    private String phone;
    private String address;
    private LocalDate birthDate;
    private String gender;
    private String nationality;
    private String position;
    private Site site;
}
