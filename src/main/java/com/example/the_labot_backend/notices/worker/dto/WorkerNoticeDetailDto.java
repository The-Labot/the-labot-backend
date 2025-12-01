package com.example.the_labot_backend.notices.worker.dto;

import com.example.the_labot_backend.files.dto.FileResponse;
import com.example.the_labot_backend.files.entity.File;
import com.example.the_labot_backend.notices.entity.Notice;
import com.example.the_labot_backend.notices.entity.NoticeCategory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class WorkerNoticeDetailDto {
    private Long id;
    private String title;
    private NoticeCategory category;
    private boolean isUrgent;
    private LocalDate createdDate;
    private String content;
    private String writerName;
    private List<FileResponse> files;

    public static WorkerNoticeDetailDto of(Notice notice, List<FileResponse> files) {
        return WorkerNoticeDetailDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .category(notice.getCategory())
                .isUrgent(notice.isUrgent())
                .createdDate(notice.getCreatedAt().toLocalDate())
                .content(notice.getContent())
                .writerName(notice.getWriter().getName())
                .files(files) // 변환 로직 필요 없음. 이미 DTO임.
                .build();
    }



}
