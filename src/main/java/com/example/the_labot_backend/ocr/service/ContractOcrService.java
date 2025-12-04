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

    private final ClovaOcrClient clovaOcrClient;

    public ContractOcrService(ClovaOcrClient clovaOcrClient) {
        this.clovaOcrClient = clovaOcrClient;
    }

    public ContractDataDto processContract(MultipartFile imageFile) {

        ClovaOcrResponseDto clovaResponse =
                clovaOcrClient.callClovaApi(imageFile, "contract");

        return parseToContractData(clovaResponse);
    }

    private ContractDataDto parseToContractData(ClovaOcrResponseDto clovaResponse) {

        if (clovaResponse.getImages() == null || clovaResponse.getImages().isEmpty()) {
            throw new RuntimeException("OCR 결과에 이미지가 없습니다.");
        }

        ContractDataDto dto = new ContractDataDto();
        ClovaOcrResponseDto.Image imageInfo = clovaResponse.getImages().get(0);
        List<ClovaOcrResponseDto.Field> fields = imageInfo.getFields();

        String templateName = "";
        if (imageInfo.getMatchedTemplate() != null) {
            templateName = imageInfo.getMatchedTemplate().getName();
        }
        boolean isMonthly = templateName.contains("monthly");

        log.info("월정제 여부 판단 결과: {}", isMonthly);

        dto.setContractType(isMonthly ? "월정제" : "일용직");

        // ---------------------------------------------------------
        // 🔥 1) 계약 기간 필드(contract_period)를 먼저 모두 합쳐서 보관
        // ---------------------------------------------------------
        StringBuilder contractPeriodBuilder = new StringBuilder();

        for (ClovaOcrResponseDto.Field f : fields) {
            if ("contract_period".equals(f.getName())) {
                if (f.getInferText() != null) {
                    contractPeriodBuilder.append(f.getInferText()).append(" ");
                }
            }
        }

        String mergedContractPeriod = contractPeriodBuilder.toString().trim();
        log.info("🔍 합쳐진 contract_period: {}", mergedContractPeriod);

        // ---------------------------------------------------------
        // 🔥 2) 임금 산정, 기타 필드는 개별적으로 처리
        // ---------------------------------------------------------
        for (ClovaOcrResponseDto.Field field : fields) {

            String fieldName = field.getName();
            String rawText = field.getInferText();
            if (rawText == null || rawText.isBlank()) continue;

            switch (fieldName) {

                case "wage_calculation_date":
                    if (isMonthly) {
                        log.info("월정제 → 임금 산정기간 무시");
                    } else {
                        log.info("일용직 → 임금 산정기간 추출");
                        LocalDate[] wageDates = parseWagePeriodSimple(rawText);
                        dto.setWageStartDate(wageDates[0]);
                        dto.setWageEndDate(wageDates[1]);
                    }
                    break;

                case "pay_receive":
                    dto.setPayReceive(rawText.replaceAll("[^0-9]", ""));
                    break;

                case "job_type":
                    dto.setJobType(rawText);
                    break;

                case "salary":
                    dto.setSalary(rawText.replaceAll("[^0-9]", ""));
                    break;

                case "site_name":
                    dto.setSiteName(rawText);
                    break;

                case "bank_name":
                    dto.setBankName(rawText);
                    break;

                case "account_holder":
                    dto.setAccountHolder(rawText);
                    break;

                case "account_number":
                    dto.setAccountNumber(rawText.replaceAll("[^0-9-]", ""));
                    break;

                case "phone_number_myself":
                    dto.setPhoneNumber(cleanPhoneNumber(rawText));
                    break;

                case "phone_number_emergency":
                    dto.setEmergencyNumber(cleanPhoneNumber(rawText));
                    break;

                default:
                    break;
            }
        }

        // ---------------------------------------------------------
        // 🔥 3) 계약기간 최종 파싱 (한 번만!)
        // ---------------------------------------------------------
        if (!mergedContractPeriod.isEmpty()) {
            LocalDate[] contractDates = parseDates(mergedContractPeriod);
            dto.setContractStartDate(contractDates[0]);
            dto.setContractEndDate(contractDates[1]);
        }

        return dto;
    }

    // ---------------------------------------------------------
    // 🔧 임금 산정 기간 (일용직)
    // ---------------------------------------------------------
    private LocalDate[] parseWagePeriodSimple(String raw) {

        LocalDate[] results = new LocalDate[2];
        LocalDate now = LocalDate.now();

        Pattern p = Pattern.compile("(\\d+)");
        Matcher m = p.matcher(raw);

        try {
            if (m.find()) {
                int day1 = Integer.parseInt(m.group(1));
                results[0] = now.minusMonths(1).withDayOfMonth(day1);
            }
            if (m.find()) {
                int day2 = Integer.parseInt(m.group(1));
                results[1] = now.withDayOfMonth(day2);
            }
        } catch (Exception e) {
            log.warn("임금 산정일 파싱 오류: {}", raw);
        }

        return results;
    }

    // ---------------------------------------------------------
    // 🔧 전화번호 정리
    // ---------------------------------------------------------
    private String cleanPhoneNumber(String raw) {
        if (raw == null) return null;
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() < 8) return raw;

        String last8 = digits.substring(digits.length() - 8);
        return "010" + last8;
    }

    // ---------------------------------------------------------
    // 🔧 계약기간 날짜 추출 (개선된 정규식)
    // ---------------------------------------------------------
    private LocalDate[] parseDates(String raw) {

        LocalDate[] results = new LocalDate[2];

        // "20 25.12.13", "2025.12.13", "20 26.1.12" 모두 가능
        Pattern datePattern =
                Pattern.compile("20\\s?(\\d{2})[^0-9]*(\\d{1,2})[^0-9]*(\\d{1,2})");

        Matcher matcher = datePattern.matcher(raw);

        int count = 0;

        while (matcher.find() && count < 2) {
            try {
                int year = Integer.parseInt(matcher.group(1));
                int month = Integer.parseInt(matcher.group(2));
                int day = Integer.parseInt(matcher.group(3));

                results[count] = LocalDate.of(2000 + year, month, day);
                count++;

            } catch (Exception e) {
                log.warn("날짜 파싱 실패: {}", matcher.group());
            }
        }

        return results;
    }
}