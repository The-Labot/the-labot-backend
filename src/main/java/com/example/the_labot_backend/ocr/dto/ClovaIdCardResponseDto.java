package com.example.the_labot_backend.ocr.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClovaIdCardResponseDto {

    private List<Image> images;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Image {
        private IdCardObject idCard;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IdCardObject {
        private Object meta; // (안정성을 위해 추가)
        private Result result;
        private String idtype; // ★★★★★ 이 필드가 JSON의 "idtype"을 읽어옴
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private IdData dl;
        private IdData rc;
        private IdData ic; // ★★★★★★★★★★★★★ 이 한 줄 추가!
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IdData {
        private List<OcrField> name;
        private List<OcrField> personalNum;
        private List<OcrField> num;
        private List<OcrField> address;
        private List<OcrField> issueDate;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OcrField {
        private String text;
        private Formatted formatted;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Formatted {
        private String value;
        private String year;
        private String month;
        private String day;
    }
}
