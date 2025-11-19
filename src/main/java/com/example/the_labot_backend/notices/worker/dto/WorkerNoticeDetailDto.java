package com.example.the_labot_backend.notices.worker.dto;

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
    private List<FileDto> files;

    @Data
    @Builder
    public static class FileDto {
        private Long fileId;
        private String originalFileName; // 화면에 보여줄 이름 (예: "안전수칙.pdf")
        private String fileUrl;          // 다운로드 링크

        public static FileDto fromEntity(File file) {
            return FileDto.builder()
                    .fileId(file.getId())
                    .originalFileName(file.getOriginalFileName())
                    .fileUrl(file.getFileUrl())
                    .build();
        }
    }

    public static WorkerNoticeDetailDto fromEntity(Notice notice, List<File> files) {
        return WorkerNoticeDetailDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .category(notice.getCategory())
                .isUrgent(notice.isUrgent())
                .createdDate(notice.getCreatedAt().toLocalDate())
                .content(notice.getContent())
                .writerName(notice.getWriter().getName())
                .files(files.stream()
                        .map(FileDto::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }

}
