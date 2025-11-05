package com.example.the_labot_backend.site;


import com.example.the_labot_backend.users.User;
import jakarta.persistence.*;
import lombok.*;

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
    private Long id;

    @Column(nullable = false)
    private String siteName; // 예: 세종 A현장

    @Column
    private String siteLocation; // 예: 세종시 조치원읍 ~

    @Column
    private String description; // 현장 설명 or 비고

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_manager_id")
    private User headManager; // 본사관리자(등록자)

    @OneToOne(mappedBy = "site", cascade = CascadeType.ALL)
    private User user;
}
