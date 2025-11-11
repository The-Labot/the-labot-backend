package com.example.the_labot_backend.hazards.entity;


import com.example.the_labot_backend.sites.Site;
import com.example.the_labot_backend.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "hazards")
public class Hazard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String hazardType; // 위험 유형
    private String location; // 위치
    private String description; // 설명
    private String fileUrl; // 파일
    private boolean urgent; // 긴급 여부
    
    @Enumerated(EnumType.STRING)
    private HazardStatus status; // 상태: WAITING, IN_PROGRESS, RESOLVED

    private LocalDateTime reportedAt; // 신고 날짜

    @ManyToOne
    @JoinColumn(name = "site_id")
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter;// 신고자
}

