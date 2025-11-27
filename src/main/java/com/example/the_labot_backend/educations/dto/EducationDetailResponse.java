package com.example.the_labot_backend.educations.dto;

import com.example.the_labot_backend.educations.entity.Education;
import com.example.the_labot_backend.educations.entity.EducationParticipant;
import com.example.the_labot_backend.educations.entity.EducationStatus;
import com.example.the_labot_backend.educations.entity.EducationType;
import com.example.the_labot_backend.files.entity.File;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class EducationDetailResponse {
    private Long id;

    // 기본 정보
    private String siteName;
    private String writerName;
    private LocalDate createdDate;

    // 교육 정보
    private String educationTitle;
    private LocalDate educationDate;
    private String educationTime;
    private String educationPlace;
    private EducationType educationType;

    private String instructor;
    private String content;
    private EducationStatus status;

    private String specialNote;   // 특이사항
    private String result;        // 교육 결과

    // 교육 대상자
    private List<ParticipantDto> participants;

    // 파일 URL
    private List<FileDto> materials;
    private List<FileDto> photos;
    private List<FileDto> signatures;

    public static EducationDetailResponse from(
            Education e,
            List<EducationParticipant> participants,
            List<File> materials,
            List<File> photos,
            List<File> signatures
    ) {
        return EducationDetailResponse.builder()
                .id(e.getId())
                .siteName(e.getSite().getProjectName())
                .writerName(e.getWriter().getName())
                .createdDate(e.getCreatedDate())
                .educationTitle(e.getEducationTitle())
                .educationDate(e.getEducationDate())
                .educationTime(e.getEducationTime())
                .educationPlace(e.getEducationPlace())
                .educationType(e.getEducationType())
                .instructor(e.getInstructor())
                .content(e.getContent())
                .status(e.getStatus())
                .result(e.getResult())
                .specialNote(e.getSpecialNote())

                .participants(
                        participants.stream()
                                .map(p -> new ParticipantDto(
                                        p.getWorker().getId(),
                                        p.getWorker().getUser().getName()
                                ))
                                .toList()
                )
                .materials(FileDto.fromList(materials))
                .photos(FileDto.fromList(photos))
                .signatures(FileDto.fromList(signatures))
                .build();
    }

    @Getter
    @AllArgsConstructor
    public static class ParticipantDto {
        private Long workerId;
        private String name;
    }

    @Getter
    @Builder
    public static class FileDto {
        private Long id;
        private String url;
        private String originalFileName;

        public static FileDto from(File f) {
            return FileDto.builder()
                    .id(f.getId())
                    .url(f.getFileUrl())
                    .originalFileName(f.getOriginalFileName())
                    .build();
        }

        public static List<FileDto> fromList(List<File> files) {
            return files.stream().map(FileDto::from).toList();
        }
    }
}
