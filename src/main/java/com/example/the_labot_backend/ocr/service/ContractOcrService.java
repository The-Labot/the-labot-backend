package com.example.the_labot_backend.ocr.service;

import com.example.the_labot_backend.ocr.dto.ClovaOcrResponseDto;
import com.example.the_labot_backend.ocr.dto.ContractDataDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ContractOcrService {
    private final ClovaOcrClient clovaOcrClient; // ★ RestTemplate 대신 주입

    // (생성자)
    public ContractOcrService(ClovaOcrClient clovaOcrClient) {
        this.clovaOcrClient = clovaOcrClient;
    }

    /**
     * "계약서" 이미지를 처리하고, "ContractDataDto"로 파싱
     */
    public ContractDataDto processContract(MultipartFile imageFile) {

        // 1. 범용 클라이언트를 호출 (템플릿명으로 "contract" 전달)
        ClovaOcrResponseDto clovaResponse = clovaOcrClient.callClovaApi(imageFile, "contract");

        // 2. 계약서용 파싱 로직 수행
        return parseToContractData(clovaResponse);
    }

    /**
     * Clova OCR 응답을 "계약서" DTO로 변환하는 헬퍼 메서드
     * (이 메서드의 내부는 변경할 필요가 없습니다)
     */
    private ContractDataDto parseToContractData(ClovaOcrResponseDto clovaResponse) {
        if (clovaResponse.getImages() == null || clovaResponse.getImages().isEmpty()) {
            throw new RuntimeException("OCR 결과에 이미지가 없습니다.");
        }

        // ★★★ 이 로그가 콘솔에 찍히는지 확인하세요 ★★★
        log.info("========= CLOVA OCR '계약서' 응답 도착 =========");

        ContractDataDto dto = new ContractDataDto();
        List<ClovaOcrResponseDto.Field> fields = clovaResponse.getImages().get(0).getFields();
        Pattern datePattern = Pattern.compile("(\\d{4}).*?(\\d{1,2}).*?(\\d{1,2})");

        for (ClovaOcrResponseDto.Field field : fields) {
            String fieldName = field.getName();
            String rawText = field.getInferText();

            // ★★★ 가장 중요한 디버깅 로그입니다. 이 로그가 콘솔에 어떻게 찍히나요? ★★★
            log.info("CLOVA 필드명: '{}', 추출된 값: '{}'", field.getName(), field.getInferText());

            switch (fieldName) {
                case "job_type":
                    dto.setJobType(field.getInferText());
                    break;
                case "salary":
                    dto.setSalary(field.getInferText());
                    break;
                case "contract_period":
                    Matcher matcher = datePattern.matcher(rawText);

                    // 1. 첫 번째 날짜(StartDate) 찾기
                    if (matcher.find()) { // "2025", "3", "21"을 찾고 멈춤
                        int year = Integer.parseInt(matcher.group(1));
                        int month = Integer.parseInt(matcher.group(2));
                        int day = Integer.parseInt(matcher.group(3));

                        dto.setContractStartDate(LocalDate.of(year, month, day));
                    }

                    // 2. 두 번째 날짜(EndDate) 찾기
                    if (matcher.find()) { // 그 다음부터 "2025", "7", "17"을 찾음
                        int year = Integer.parseInt(matcher.group(1));
                        int month = Integer.parseInt(matcher.group(2));
                        int day = Integer.parseInt(matcher.group(3));

                        dto.setContractEndDate(LocalDate.of(year, month, day));
                    }
                    break;
                case "phone_number": // TODO: 실제 템플릿 필드명으로 변경
                    String rawPhoneNumber = field.getInferText();

                    // 2. [★ 수정 ★] replaceAll을 사용해 모든 공백(\s)을 빈 문자열("")로 바꿉니다.
                    String cleanPhoneNumber = rawPhoneNumber.replaceAll("\\s", ""); // "01012345678"

                    // 3. 띄어쓰기가 제거된 '문자열'을 DTO에 저장합니다.
                    dto.setPhoneNumber(cleanPhoneNumber);
                    break;
            }
        }
        return dto; // <-- switch 문에
    }
}
