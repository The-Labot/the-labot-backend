package com.example.the_labot_backend.admins.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SiteManagerResponse {
    private Long id;
    private String name;
    private String phone;
}