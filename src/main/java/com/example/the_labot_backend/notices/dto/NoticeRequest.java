package com.example.the_labot_backend.notices.dto;

import com.example.the_labot_backend.notices.entity.NoticeCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NoticeRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotNull
    private NoticeCategory category;

    private String fileUrl; // 첨부파일 (선택)

    private boolean urgent; // 긴급 여부
    private boolean pinned; // 고정 여부
}
