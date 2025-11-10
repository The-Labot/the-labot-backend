package com.example.the_labot_backend.notices.dto;

import com.example.the_labot_backend.notices.entity.Notice;
import com.example.the_labot_backend.notices.entity.NoticeCategory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// 공지사항 목록 응답
@Getter
@Builder
public class NoticeListResponse {

    private Long id;
    private String title;
    private NoticeCategory category;
    private boolean urgent;
    private boolean pinned;
    private String writer;
    private LocalDateTime createdAt;
}