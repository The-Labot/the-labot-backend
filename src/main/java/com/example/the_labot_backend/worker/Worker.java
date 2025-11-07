package com.example.the_labot_backend.worker;

import com.example.the_labot_backend.enums.WorkerStatus;
import com.example.the_labot_backend.users.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "workers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Worker {

    @Id
    private Long id; 

    @OneToOne
    // Worker의 id와 User의 id를 동일하게 만들어줌
    // 엔티티의 수명을 공유함. 항상 같이 존재. User가 없는 Worker는 존재할 수 없기 때문에.
    // Worker가 저장될 때 user_id값이 id에 들어가 똑같아짐.
    @MapsId
    @JoinColumn(name = "user_id") // 외래키 컬럼명
    private User user;

    private String address; // 주소
    private String gender; // 성별
    private LocalDate birthDate; // 생년월일
    private String nationality; // 국적

    @Enumerated(EnumType.STRING)
    private WorkerStatus status; // 근무중, 대기중, 퇴직

    private String position; // 직종 (배관공, 전기공 등)
    private String siteName; // 상세 근무 현장명

    private String profileImage; // 프로필 이미지 (optional)
}
