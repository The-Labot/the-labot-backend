package com.example.the_labot_backend.notices.dto;

import com.example.the_labot_backend.notices.entity.NoticeCategory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NoticeResponse {

    private Long id;
    private String title;
    private String content;
    private String fileUrl;
    private NoticeCategory category;
    private boolean urgent;
    private boolean pinned;
    private String writerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}