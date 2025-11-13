package com.example.the_labot_backend.ocr.service;

import com.example.the_labot_backend.ocr.dto.ClovaIdCardResponseDto;
import com.example.the_labot_backend.ocr.dto.IdCardDataDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
public class IdCardOcrService {
    private final ClovaOcrClient clovaOcrClient;
    private final ObjectMapper objectMapper; // ★ JSON 파싱용

    // ★ 생성자에서 ClovaOcrClient와 ObjectMapper를 주입받습니다.
    public IdCardOcrService(ClovaOcrClient clovaOcrClient, ObjectMapper objectMapper) {
        this.clovaOcrClient = clovaOcrClient;
        this.objectMapper = objectMapper;
    }
    // processIdCard (기존과 동일)
    public IdCardDataDto processIdCard(MultipartFile imageFile) {
        try {
            String jsonResponse = clovaOcrClient.callIdCardApi(imageFile);
            log.info("CLOVA ID CARD 원본 응답: {}", jsonResponse);

            // [★ 수정된 DTO]를 사용해서 JSON을 받음
            ClovaIdCardResponseDto clovaResponse = objectMapper.readValue(jsonResponse, ClovaIdCardResponseDto.class);

            return parseToIdCardData(clovaResponse);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("ID Card JSON 파싱 또는 처리 중 오류", e);
        }
    }

    /**
     * "신분증" 이미지를 처리하고, "IdCardDataDto"로 파싱
     */
    /**
     * [★ 수정됨 2.0 ★]
     * 신분증 종류(dl/rc)에 따라 분기 처리하여 파싱
     */
    /**
     * [★ 최종-단순화 ★]
     * 이름, 주소, 주민번호 "만" 뽑아내는 로직
     */
    /**
     * [★ 최종 수정: 'ic' 필드 추가 ★]
     * 이름, 주소, 주민번호 "만" 뽑아내는 로직
     */
    private IdCardDataDto parseToIdCardData(ClovaIdCardResponseDto clovaResponse) {
        // --- 1. 유효성 검사 ---
        if (clovaResponse.getImages() == null || clovaResponse.getImages().isEmpty() ||
                clovaResponse.getImages().get(0).getIdCard() == null ||
                clovaResponse.getImages().get(0).getIdCard().getResult() == null) {
            throw new RuntimeException("인식된 신분증 정보(idCard.result)가 없습니다.");
        }

        log.info("========= CLOVA '신분증' 파싱 시작 (필수 3종) =========");

        IdCardDataDto dto = new IdCardDataDto();
        ClovaIdCardResponseDto.Result result = clovaResponse.getImages().get(0).getIdCard().getResult();

        // ★★★ 핵심 수정 ★★★
        ClovaIdCardResponseDto.IdData data;
        if (result.getDl() != null) {
            log.info("운전면허증(dl) 데이터 감지");
            data = result.getDl();
        } else if (result.getRc() != null) {
            log.info("주민등록증(rc) 데이터 감지");
            data = result.getRc();
        } else if (result.getIc() != null) { // ▼▼▼▼▼▼▼▼▼▼ [이 줄 추가!] ▼▼▼▼▼▼▼▼▼▼
            log.info("주민등록증(ic) 데이터 감지");
            data = result.getIc(); //             [이 줄 추가!]
        } else { // ▲▲▲▲▲▲▲▲▲▲ [이 줄 추가!] ▲▲▲▲▲▲▲▲▲▲
            throw new RuntimeException("dl, rc, ic 데이터를 모두 찾을 수 없습니다."); // (에러 메시지 수정)
        }

        // --- 2. 필요한 3개 값만 매핑 ---
        dto.setName(getFirstFormattedValue(data.getName()));
        dto.setAddress(getFirstTextValue(data.getAddress()));
        dto.setResidentIdNumber(getFirstFormattedValue(data.getPersonalNum()));

        log.info("========= CLOVA '신분증' 파싱 완료 (결과: {}) =========" , dto);
        return dto;
    }

    /**
     * (헬퍼) OcrField 리스트에서 첫 번째 'formatted.value' 추출
     */
    // --- 헬퍼 메서드 (기존과 동일) ---

    private String getFirstFormattedValue(List<ClovaIdCardResponseDto.OcrField> fieldList) {
        if (fieldList == null || fieldList.isEmpty() || fieldList.get(0).getFormatted() == null) {
            return null;
        }
        return fieldList.get(0).getFormatted().getValue();
    }

    private String getFirstTextValue(List<ClovaIdCardResponseDto.OcrField> fieldList) {
        if (fieldList == null || fieldList.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (ClovaIdCardResponseDto.OcrField field : fieldList) {
            sb.append(field.getText()).append(" ");
        }
        return sb.toString().trim();
    }

    /**
     * [★ 신규 헬퍼 ★]
     * 날짜(issueDate) 필드에서 YYYYMMDD 형식으로 추출
     */
}
