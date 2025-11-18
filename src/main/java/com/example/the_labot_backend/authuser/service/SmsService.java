package com.example.the_labot_backend.authuser.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SmsService {

    @Value("${solapi.api-key}")
    private String apiKey;

    @Value("${solapi.api-secret}")
    private String apiSecret;

    @Value("${solapi.sender-number}")
    private String senderNumber;

    @Value("${solapi.api-url}")
    private String API_URL;

    public void sendSms(String to, String text) {

        try {
            // ─────────────────────────────────────────────
            // 1) Authorization 헤더 생성 (공식 방식)
            // ─────────────────────────────────────────────
            String authHeader = SolapiAuth.createAuthHeader(apiKey, apiSecret);

            // ─────────────────────────────────────────────
            // 2) 헤더 설정
            // ─────────────────────────────────────────────
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", authHeader);

            // ─────────────────────────────────────────────
            // 3) Body (메시지 JSON)
            // ─────────────────────────────────────────────
            Map<String, Object> message = new HashMap<>();
            message.put("to", to);
            message.put("from", senderNumber);
            message.put("text", text);

            Map<String, Object> body = new HashMap<>();
            body.put("message", message);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            // ─────────────────────────────────────────────
            // 4) 요청 전송
            // ─────────────────────────────────────────────
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response =
                    restTemplate.postForEntity(API_URL, entity, String.class);

            // ─────────────────────────────────────────────
            // 5) 응답 검증
            // ─────────────────────────────────────────────
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("SMS 발송 실패: " + response.getBody());
            }

        } catch (Exception e) {
            throw new RuntimeException("SMS 발송 중 오류 발생", e);
        }
    }
}
