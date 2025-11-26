package com.example.the_labot_backend.admins.entity;

import com.example.the_labot_backend.authuser.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "admins")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Admin {

    @Id
    private Long id;            // 관리자 ID

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, unique = true, length = 100)
    private String email;       // 이메일

    @Column(length = 255)
    private String address;     // 주소

}

// 이름 생년월일, 이메일, 비밀번호, 전화번호, 주소, 약관동의