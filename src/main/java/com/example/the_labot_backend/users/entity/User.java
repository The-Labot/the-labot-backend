package com.example.the_labot_backend.users.entity;

import com.example.the_labot_backend.admins.Admin;
import com.example.the_labot_backend.headoffice.HeadOffice;
import com.example.the_labot_backend.sites.Site;
import com.example.the_labot_backend.workers.Worker;
import com.fasterxml.jackson.annotation.JsonIgnore; //user와 worker간의 루프를 해결하기 위해 생성 11/13 7시반
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor // 파라미터 없는 기본 생성자 자동 생성
@AllArgsConstructor // 모든 필드를 매개변수로 받는 생성자 생성
@Builder // 빌더 패턴
@Table(name = "users") // DB 테이블 이름 명시
public class User {

    @Id // 기본키 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 자동 생성 전략 지정
    private Long id;

    @Column(nullable = false, unique = true) // DB 컬럼 설정을 세밀하게 제어
    private String phoneNumber; // 로그인용 전화번호

    @Column(nullable = false)
    private String password;

    private String name;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "site_id")
    private Site site;  // 소속 현장 (현장관리자, 현장근로자)

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "hd_id")
    private HeadOffice headOffice; // 소속 본사 (본사근로자)

    @Enumerated(EnumType.STRING) // Enum타입을 DB에 저장할 때 사용
    @Column(nullable = false)
    private Role role; // ex) ROLE_USER, ROLE_WORKER, ROLE_ADMIN

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL) // 1대1 대응, mappedBy: 연관관계의 반대편 변수 이름 (user로 되어있음), cascade: 삭제전략
    private Admin admin;  // 근로자 전용 정보 연결

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL) // 1대1 대응, mappedBy: 연관관계의 반대편 변수 이름 (user로 되어있음), cascade: 삭제전략
    @JsonIgnore//user와 worker간의 루프를 해결하기 위해 생성 11/13 7시반 박찬홍 추가
    private Worker worker;  // 근로자 전용 정보 연결
}
