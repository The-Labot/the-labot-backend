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


    public ContractOcrService(ClovaOcrClient clovaOcrClient) {
        this.clovaOcrClient = clovaOcrClient;
    }


    public ContractDataDto processContract(MultipartFile imageFile) {

        // 1. 범용 클라이언트를 호출 (템플릿명으로 "contract" 전달)
        ClovaOcrResponseDto clovaResponse = clovaOcrClient.callClovaApi(imageFile, "contract");

        // 2. 계약서용 파싱 로직 수행
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
        boolean isMonthly = false; // 기본값은 일용직(일정제)
        for (ClovaOcrResponseDto.Field field : fields) {
            // 필드 이름이 contract_type이고, 내용에 "월정제"가 포함되어 있으면 -> 월정제로 판단
            if ("contract_type".equals(field.getName())) {
                String val = field.getInferText();
                if (val != null && val.contains("월정제")) {
                    isMonthly = true;
                }
                break; // 판단 끝났으면 루프 중단
            }
        }

        log.info("월정제 여부 판단 결과: {}", isMonthly);

        // 2. 판단 결과에 따라 계약 형태 저장
        if (isMonthly) {
            dto.setContractType("월정제");
        } else {
            dto.setContractType("일용직");
        }

        for (ClovaOcrResponseDto.Field field : fields) {
            String fieldName = field.getName();
            String rawText = field.getInferText();
            if (rawText == null || rawText.isBlank()) continue;
            switch (fieldName) {
                case "wage_calculation_date":
                    if (isMonthly) {
                        // [★ 로직 변경] 월정제: 임금 산정일 필요 없음 (Pass)
                        log.info(">> 월정제: 임금 산정일 데이터 무시함");
                    } else {
                        // [★ 로직 변경] 일용직: 산정 기간(시작~종료) 추출해서 저장
                        log.info(">> 일용직: 임금 산정 기간 추출");
                        LocalDate[] wageDates = parseWagePeriodSimple(rawText);
                        dto.setWageStartDate(wageDates[0]);
                        dto.setWageEndDate(wageDates[1]);
                    }
                    break;
                case "pay_receive":
                    // "매월 15일" -> "15" (숫자만 추출)
                    dto.setPayReceive(rawText.replaceAll("[^0-9]", ""));
                    break;
                // ========================================================
                //  공통 필드 (직종, 급여, 은행 등 로직이 같다면 그냥 둠)
                // ========================================================
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

                // ========================================================
                //  ★ 분기가 필요한 필드들 (여기서 isMonthly로 가름)
                // ========================================================

                case "contract_period":
                    // 만약 근로기간도 월정제가 다르다면 여기에 if(isMonthly) 쓰면 됨
                    // (지금은 공통 로직으로 유지, 필요시 수정 가능)
                    LocalDate[] dates = parseDates(rawText);
                    dto.setContractStartDate(dates[0]);
                    dto.setContractEndDate(dates[1]);
                    break;




            }
        }
        return dto;



    }

    private LocalDate[] parseWagePeriodSimple(String raw) {
        LocalDate[] results = new LocalDate[2];
        LocalDate now = LocalDate.now(); // 기준: 오늘 (앱 등록 시점)

        // 숫자만 추출하는 정규식
        Pattern p = Pattern.compile("(\\d+)");
        Matcher m = p.matcher(raw);

        try {
            // 1. 첫 번째 숫자 발견 -> 지난달(Month - 1)
            if (m.find()) {
                int day1 = Integer.parseInt(m.group(1));
                results[0] = now.minusMonths(1).withDayOfMonth(day1);
            }
            // 2. 두 번째 숫자 발견 -> 이번달(Current Month)
            if (m.find()) {
                int day2 = Integer.parseInt(m.group(1));
                results[1] = now.withDayOfMonth(day2);
            }
        } catch (Exception e) {
            log.warn("임금 산정일 파싱 중 날짜 오류 발생 (값: {})", raw);
            // 날짜가 유효하지 않은 경우(예: 지난달에 31일이 없는데 31일 입력 등) null 반환될 수 있음
        }
        return results;
    }
    private String cleanPhoneNumber(String raw) {
        if (raw == null) return null;
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() < 8) return raw;

        String last8 = digits.substring(digits.length() - 8);
        return "010" + last8;
    }
    private LocalDate[] parseDates(String raw) {
        LocalDate[] results = new LocalDate[2];
        Pattern datePattern = Pattern.compile("(\\d{4})[^0-9]*(\\d{1,2})[^0-9]*(\\d{1,2})");
        Matcher matcher = datePattern.matcher(raw);

        int count = 0;
        while (matcher.find() && count < 2) {
            try {
                int y = Integer.parseInt(matcher.group(1));
                int m = Integer.parseInt(matcher.group(2));
                int d = Integer.parseInt(matcher.group(3));
                results[count] = LocalDate.of(y, m, d);
                count++;
            } catch (Exception e) {
                // ignore
            }
        }
        return results;
    }


}
