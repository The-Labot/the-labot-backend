package com.example.the_labot_backend.ocr.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClovaOcrResponseDto {
    private List<Image> images;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Image {
        private MatchedTemplate matchedTemplate; // 템플릿 이름 확인용 월정제 or not
        private List<Field> fields;
    }
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MatchedTemplate {

        private String name; // 예: "월정제_근로계약서" 또는 "일반_근로계약서"
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Field {
        private String name;       // 템플릿 빌더에서 설정한 '필드명' (예: "job_type")
        private String inferText;  // OCR이 추출한 실제 텍스트 값 (예: "현장직")
    }
}
