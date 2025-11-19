package com.example.the_labot_backend.headoffice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "head_office")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class HeadOffice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String secretCode;      // 본사 비밀번호

    // 본사코드(식별자), 본사명, 대표자명, 사업자등록번호, 주소, 대표 전화번호, 대표 이메일, 설명
    @Column(nullable = false)
    private String name;            // 본사 이름
    private String address;         // 주소
    private String phoneNumber;     // 대표 연락처
    private String representative;  // 대표자명
}
