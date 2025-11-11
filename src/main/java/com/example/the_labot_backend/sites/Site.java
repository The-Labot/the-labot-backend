package com.example.the_labot_backend.sites;


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
//임시 현장 클래스
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 현장 ID

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
}
