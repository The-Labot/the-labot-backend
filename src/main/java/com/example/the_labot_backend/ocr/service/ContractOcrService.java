package com.example.the_labot_backend.ocr.service;

import com.example.the_labot_backend.ocr.dto.ClovaOcrResponseDto;
import com.example.the_labot_backend.ocr.dto.ContractDataDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
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
        boolean isMonthly = templateName.contains("monthly");


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
            String cleanNumber = rawText.replaceAll("[^0-9]", "");
            switch (fieldName) {
                case "contractStartDate":
                    dto.setContractStartDate(parseDateFromMixedString(rawText));
                    break;

                case "contractEndDate":
                    dto.setContractEndDate(parseDateFromMixedString(rawText));
                    break;




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
                    // 예: "(은행 국민" -> "국민"
                    // 예: "(은행:농협" -> "농협"
                    // 예: "신한은행" -> "신한은행" (유지됨)

                    String rawBank = rawText;

                    // 1. "(은행" 이라는 라벨 텍스트가 섞여 있다면 제거
                    if (rawBank.contains("(은행")) {
                        rawBank = rawBank.replace("(은행", "");
                    }

                    // 2. 라벨 제거 후 남은 특수문자(:, (, )) 및 공백 제거
                    // [:()] -> 콜론, 여는괄호, 닫는괄호
                    String cleanBank = rawBank.replaceAll("[:()]", "").trim();

                    dto.setBankName(cleanBank);
                    break;

                case "account_holder":
                    String cleanHolder = rawText.replaceAll("[:.]", "").trim();
                    dto.setAccountHolder(cleanHolder);
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







            }
        }

        


        return dto;



    }
    private LocalDate parseDateFromMixedString(String raw) {
        if (raw == null || raw.isBlank()) return null;

        // 디버깅을 위해 로그를 꼭 확인해보세요!
        log.info(">> 날짜 파싱 시도 (원본): [{}]", raw);

        // 1. 문자열에서 "숫자"만 모두 추출하여 리스트에 담기
        // 예: "2. 근로계약 시작일: 2025. 09. 01." -> [2, 2025, 9, 1]
        // 예: "25/10/15" -> [25, 10, 15]
        List<Integer> numbers = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\d+").matcher(raw);
        while (matcher.find()) {
            numbers.add(Integer.parseInt(matcher.group()));
        }

        // 2. 숫자 리스트를 순회하며 "연-월-일" 패턴 찾기
        // (숫자가 최소 3개 이상이어야 날짜 구성 가능)
        for (int i = 0; i <= numbers.size() - 3; i++) {
            int y = numbers.get(i);
            int m = numbers.get(i + 1);
            int d = numbers.get(i + 2);

            // [검증 1] 월(Month)은 1~12 사이여야 함
            if (m < 1 || m > 12) continue;

            // [검증 2] 일(Day)은 1~31 사이여야 함
            if (d < 1 || d > 31) continue;

            // [검증 3] 연도(Year) 보정 및 확인
            // 경우 A: 4자리 연도 (예: 2025) -> 그대로 사용
            // 경우 B: 2자리 연도 (예: 25) -> 2000 더하기
            if (y < 100) {
                y += 2000;
            }

            // 연도가 너무 작거나(1900년 미만) 너무 크면 날짜 아님 -> 패스
            // (앞에 붙은 '2.' 같은 목차 번호를 여기서 걸러냄)
            if (y < 2000 || y > 2100) continue;

            try {
                // 유효한 날짜 찾음 -> 즉시 리턴
                LocalDate result = LocalDate.of(y, m, d);
                log.info(">> 날짜 파싱 성공: {}", result);
                return result;
            } catch (Exception e) {
                // 날짜 생성 실패 (예: 2월 30일 등) -> 다음 조합 검색
                continue;
            }
        }

        log.warn(">> 날짜 패턴을 찾을 수 없음: {}", raw);
        return null;
    }

    // 2자리 연도(예: 25.10.10) 처리를 위한 보조 메서드
    private LocalDate parseShortYearDate(String raw) {
        try {
            String[] parts = raw.replaceAll("[^0-9]+", " ").trim().split(" ");
            if (parts.length >= 3) {
                int y = Integer.parseInt(parts[0]);
                int m = Integer.parseInt(parts[1]);
                int d = Integer.parseInt(parts[2]);

                // 연도가 100 미만이면 2000년대라고 가정
                if (y < 100) y += 2000;

                return LocalDate.of(y, m, d);
            }
        } catch (Exception e) {
            log.warn("2자리 연도 파싱 실패: {}", raw);
        }
        return null;
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



}
