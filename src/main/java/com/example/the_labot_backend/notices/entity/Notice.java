package com.example.the_labot_backend.notices.entity;

import com.example.the_labot_backend.sites.Site;
import com.example.the_labot_backend.authUser.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notices")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 공지사항 ID

    @Column(nullable = false, length = 100)
    private String title; // 제목

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 내용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoticeCategory category; // 공지 카테고리 (예: SAFETY, SITE 등)

    @Column(nullable = false)
    private boolean urgent; // 긴급 여부

    @Column(nullable = false)
    private boolean pinned; // 상단 고정 여부

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private User writer; // 작성자

    // 현장별 공지 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Site site;

    private LocalDateTime createdAt; // 작성일시
    private LocalDateTime updatedAt; // 수정일시

    // 최초 저장 시 자동으로 생성일 설정
    @PrePersist // 처음 저장 시 createAt 자동 세팅
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // 수정 시 자동으로 수정일 갱신
    @PreUpdate // 수정할 때 마다 updateAt 자동 갱신
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 수정 메서드
    public void update(String title, String content, NoticeCategory category, boolean urgent, boolean pinned) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.urgent = urgent;
        this.pinned = pinned;
    }
}
