package com.example.the_labot_backend.admins;

import com.example.the_labot_backend.headoffice.HeadOffice;

import com.example.the_labot_backend.users.entity.User;
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

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id") // 외래키 컬럼명
    private User user;

    @Column(nullable = false, unique = true, length = 100)
    private String email;       // 이메일

    @Column(length = 255)
    private String address;     // 주소

}

// 이름 생년월일, 이메일, 비밀번호, 전화번호, 주소, 약관동의