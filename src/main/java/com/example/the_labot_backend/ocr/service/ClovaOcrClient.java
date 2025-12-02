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

    public ClovaOcrResponseDto callClovaApi(MultipartFile imageFile, String templateName) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("X-OCR-SECRET", secretKey);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            // message
            Map<String, Object> message = new HashMap<>();
            message.put("version", "V2");
            message.put("requestId", UUID.randomUUID().toString());
            message.put("timestamp", System.currentTimeMillis());

            String format = getFileExtension(imageFile.getOriginalFilename());

            Map<String, String> imageInfo = new HashMap<>();
            imageInfo.put("format", format);
            imageInfo.put("name", templateName);
            message.put("images", List.of(imageInfo));

            // [★ 핵심 변경] 단순히 String만 넣는 게 아니라, Headers를 포함한 HttpEntity로 포장
            HttpHeaders jsonHeaders = new HttpHeaders();
            jsonHeaders.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> messageEntity =
                    new HttpEntity<>(objectMapper.writeValueAsString(message), jsonHeaders);

            body.add("message", messageEntity);

            // file
            ByteArrayResource fileResource = new ByteArrayResource(imageFile.getBytes()) {
                @Override
                public String getFilename() {
                    return imageFile.getOriginalFilename();
                }
            };
            body.add("file", fileResource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(apiUrl, requestEntity, String.class);

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

            // 1) file 파트
            ByteArrayResource fileResource = new ByteArrayResource(imageFile.getBytes()) {
                @Override
                public String getFilename() {
                    return imageFile.getOriginalFilename();
                }
            };
            body.add("file", fileResource);

            // 2) message 파트 → ★ images 반드시 포함해야 함!
            Map<String, Object> message = new HashMap<>();
            message.put("version", "V2");
            message.put("requestId", UUID.randomUUID().toString());
            message.put("timestamp", System.currentTimeMillis());

            Map<String, String> imageInfo = new HashMap<>();
            imageInfo.put("format", getFileExtension(imageFile.getOriginalFilename()));
            imageInfo.put("name", "id-card");

            message.put("images", List.of(imageInfo));

            HttpHeaders jsonHeaders = new HttpHeaders();
            jsonHeaders.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> messagePart =
                    new HttpEntity<>(objectMapper.writeValueAsString(message), jsonHeaders);

            body.add("message", messagePart);

            // 최종 요청
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
