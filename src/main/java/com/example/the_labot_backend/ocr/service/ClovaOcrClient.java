package com.example.the_labot_backend.ocr.service;

import com.example.the_labot_backend.ocr.dto.ClovaOcrResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ClovaOcrClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ncp.ocr.invoke.url}")
    private String apiUrl;
    @Value("${ncp.ocr.secret.key}")
    private String secretKey;

    // vvvvvvvvvv ★ 1단계에서 추가한 신분증 API 정보 vvvvvvvvvv
    @Value("${ncp.ocr.idcard.invoke.url}")
    private String idCardApiUrl;
    @Value("${ncp.ocr.idcard.secret.key}")
    private String idCardSecretKey;

    // (생성자)
    public ClovaOcrClient(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    /**
     * Clova OCR API를 호출하는 범용 메서드
     * @param imageFile 업로드된 이미지 파일
     * @param templateName 사용할 템플릿 이름 (예: "contract", "id_card")
     * @return ClovaOcrResponseDto 원본 API 응답
     */
    public ClovaOcrResponseDto callClovaApi(MultipartFile imageFile, String templateName) {
        try {
            // 1. HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("X-OCR-SECRET", secretKey);

            // 2. HTTP 바디(Body) 생성
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // 2-1. 'message' JSON 파트 생성
            Map<String, Object> message = new HashMap<>();
            message.put("version", "V2");
            message.put("requestId", UUID.randomUUID().toString());
            message.put("timestamp", System.currentTimeMillis());


            String format = getFileExtension(imageFile.getOriginalFilename());


            Map<String, String> imageInfo = new HashMap<>();
            imageInfo.put("format", format); // ★ 수정된 format 사용
            imageInfo.put("name", templateName);
            message.put("images", List.of(imageInfo));

            body.add("message", objectMapper.writeValueAsString(message));

            // 2-2. 'file' 바이너리 파트 생성
            ByteArrayResource fileResource = new ByteArrayResource(imageFile.getBytes()) {
                @Override
                public String getFilename() {
                    return imageFile.getOriginalFilename();
                }
            };
            body.add("file", fileResource);

            // 3. API 호출
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);

            // 4. 결과 파싱
            if (response.getStatusCode() == HttpStatus.OK) {
                return objectMapper.readValue(response.getBody(), ClovaOcrResponseDto.class);
            } else {
                throw new RuntimeException("OCR API 호출 실패: " + response.getStatusCode());
            }

        } catch (Exception e) {
            throw new RuntimeException("OCR 처리 중 예외 발생", e);
        }
    }

    public String callIdCardApi(MultipartFile imageFile) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("X-OCR-SECRET", idCardSecretKey);

            LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // 1) file
            ByteArrayResource fileResource = new ByteArrayResource(imageFile.getBytes()) {
                @Override
                public String getFilename() {
                    return imageFile.getOriginalFilename();
                }
            };
            body.add("file", fileResource);

            // 2) message (Content-Type: application/json 필수)
            Map<String, Object> message = new HashMap<>();
            message.put("version", "V2");
            message.put("requestId", UUID.randomUUID().toString());
            message.put("timestamp", System.currentTimeMillis());

            HttpHeaders jsonHeaders = new HttpHeaders();
            jsonHeaders.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> messagePart =
                    new HttpEntity<>(objectMapper.writeValueAsString(message), jsonHeaders);

            body.add("message", messagePart);

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(idCardApiUrl, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("ID Card OCR API 호출 실패: " + response.getStatusCode());
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("ID Card OCR 처리 중 예외 발생", e);
        }
    }


    /**
     * [★ 신규 헬퍼 ★]
     * 파일 이름(예: "my_card.png")에서 확장자(예: "png")를 추출하는 메서드
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            // 확장자가 없는 경우, Clova가 허용하는 기본값 "jpg"로 시도
            return "jpg";
        }
        // 마지막 '.' 뒤의 문자열을 소문자로 반환
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
