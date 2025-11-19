package com.example.the_labot_backend.sites.entity;

import com.example.the_labot_backend.headoffice.entity.HeadOffice;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "sites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 현장 ID

    @ManyToOne
    @JoinColumn(name = "headoffice_id")
    private HeadOffice headOffice;

    @Column(nullable = false)
    private String siteName; // 현장명

    @Column(nullable = false)
    private String siteAddress; // 현장 주소

    @Column(length = 1000)
    private String description; // 현장 설명

    private LocalDate startDate; // 공사 시작일
    private LocalDate endDate;   // 공사 종료일

    private String constructionType; // 공사규모/타입 (예: 대형주택, 도로공사, 아파트 등)
    private String client;           // 발주처
    private String contractor;       // 시공사

    // gps이용을 위해서 추가함. 11/17 박찬홍
    @Column(nullable = false) // (출퇴근 기능이 필수라면 false로 설정)
    private Double latitude; // 현장 위도 (기준점)

    @Column(nullable = false)
    private Double longitude; // 현장 경도 (기준점)
}
