package com.example.the_labot_backend.authuser.dto;

import com.example.the_labot_backend.authuser.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ManagerListResponse {
    private Long userId;       // 관리자 고유 ID
    private String name;       // 이름
    private String phoneNumber;// 전화번호

    public static ManagerListResponse from(User user) {
        return ManagerListResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}
