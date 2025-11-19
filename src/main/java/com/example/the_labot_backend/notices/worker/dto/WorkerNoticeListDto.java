package com.example.the_labot_backend.notices.worker.dto;

import com.example.the_labot_backend.notices.entity.Notice;
import com.example.the_labot_backend.notices.entity.NoticeCategory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class WorkerNoticeListDto {
    private Long id;
    private String title;
    private NoticeCategory category;
    private LocalDate date;
    private boolean isPinned;
    private boolean isUrgent;
    private boolean isNew;

    public static WorkerNoticeListDto fromEntity(Notice notice) {
        return WorkerNoticeListDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .category(notice.getCategory())
                .date(notice.getCreatedAt().toLocalDate())
                .isPinned(notice.isPinned())
                .isUrgent(notice.isUrgent())
                .isNew(notice.getCreatedAt().isAfter(LocalDateTime.now().minusDays(3)))
                .build();
    }
}
