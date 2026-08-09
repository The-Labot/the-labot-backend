package com.example.the_labot_backend.files.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 사용자가 업로드한 원본 파일명
    @Column(nullable = false)
    private String originalFileName;

    // 서버에 저장되는 실제 파일명 (UUID로 변환)
    //url 길이 넉넉히 잡기 (varchar)
    @Column(nullable = false, length = 2048)
    private String storedFileName;

    // 접근 가능한 URL (ex. /uploads/uuid_파일명)
    @Column(nullable = false, length = 2048)
    private String fileUrl;

    private String contentType;  // MIME 타입
    private Long size;           // 파일 크기 (byte)

    // 어떤 도메인에 속하는 파일인지 구분용
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FileTargetType targetType;

    private Long targetId;     // targetType이 가리키는 도메인의 id (예: 공지사항 id)

}