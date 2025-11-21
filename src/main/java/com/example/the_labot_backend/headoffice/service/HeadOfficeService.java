package com.example.the_labot_backend.headoffice.service;

import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.headoffice.dto.HeadOfficeCheckResponse;
import com.example.the_labot_backend.headoffice.dto.HeadOfficeRequest;
import com.example.the_labot_backend.headoffice.dto.HeadOfficeResponse;
import com.example.the_labot_backend.headoffice.entity.HeadOffice;
import com.example.the_labot_backend.headoffice.repository.HeadOfficeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class HeadOfficeService {

    private final HeadOfficeRepository headOfficeRepository;
    private final UserRepository userRepository;

    // 본사 등록
    public HeadOfficeResponse createHeadOffice(HeadOfficeRequest request) {

        // 본사코드 8자리 생성
        String code = UUID.randomUUID().toString().substring(0, 8);

        HeadOffice office = HeadOffice.builder()
                .name(request.getName())
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .representative(request.getRepresentative())
                .secretCode(code)
                .build();

        headOfficeRepository.save(office);

        return HeadOfficeResponse.from(office);
    }

    // 본사코드로 본사 등록
    // 본사가 존재할 경우 true와 본사명 반환
    // 본사가 없을경우 false 반환
    @Transactional
    public HeadOfficeCheckResponse checkHeadOffice(Long userId, String secretCode) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.(getHeadOffice) userId:" + userId));

        HeadOffice office = headOfficeRepository.findBySecretCode(secretCode)
                .orElseThrow(() -> new RuntimeException("본사를 찾을 수 없습니다."));

        user.setHeadOffice(office);

        return headOfficeRepository
                .findBySecretCode(secretCode)
                .map(ho -> new HeadOfficeCheckResponse(true, ho.getName()))
                .orElseGet(() -> new HeadOfficeCheckResponse(false, null));
    }

    // 본사가 등록되어 있는 지 여부 확인 로직
    public boolean hasHeadOffice(Long userId) {
        // 방법 A: HeadOffice 테이블에 관리자(Admin) ID로 매핑되어 있는 경우
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.(getHeadOffice) userId:" + userId));

        return user.getHeadOffice() != null;
    }

    // userId를 통해 본사 상세 조회
    public HeadOfficeResponse getHeadOffice(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.(getHeadOffice) userId:" + userId));

        Long officeId = user.getHeadOffice().getId();

        HeadOffice office = headOfficeRepository.findById(officeId)
                .orElseThrow(() -> new RuntimeException("본사를 찾을 수 없습니다."));

        return HeadOfficeResponse.from(office);
    }

    // 본사 수정
    public HeadOfficeResponse updateHeadOffice(Long userId, HeadOfficeRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.(getHeadOffice) userId:" + userId));

        Long officeId = user.getHeadOffice().getId();

        HeadOffice office = headOfficeRepository.findById(officeId)
                .orElseThrow(() -> new RuntimeException("본사를 찾을 수 없습니다."));

        office.setName(request.getName());
        office.setAddress(request.getAddress());
        office.setRepresentative(request.getRepresentative());
        office.setPhoneNumber(request.getPhoneNumber());

        return HeadOfficeResponse.from(headOfficeRepository.save(office));
    }

}