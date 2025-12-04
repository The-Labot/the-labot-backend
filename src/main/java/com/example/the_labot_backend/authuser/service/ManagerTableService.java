package com.example.the_labot_backend.authuser.service;

import com.example.the_labot_backend.authuser.dto.ManagerListResponse;
import com.example.the_labot_backend.authuser.entity.Role;
import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.global.exception.BadRequestException;
import com.example.the_labot_backend.global.exception.ForbiddenException;
import com.example.the_labot_backend.global.exception.NotFoundException;
import com.example.the_labot_backend.headoffice.entity.HeadOffice;
import com.example.the_labot_backend.sites.entity.Site;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManagerTableService {

    private final UserRepository userRepository;

    /**
     * 나와 같은 현장, 같은 본사의 동료 관리자 목록 조회
     */
    public List<ManagerListResponse> getCoManagers(Long currentManagerId) {
        // 1. 현재 요청한 관리자 정보 조회
        User currentManager = userRepository.findById(currentManagerId)
                .orElseThrow(() -> new NotFoundException("사용자 정보를 찾을 수 없습니다."));

        // 2. 관리자 권한 체크 (안전장치)
        if (currentManager.getRole() != Role.ROLE_MANAGER) {
            throw new ForbiddenException("현장 관리자만 조회할 수 있습니다.");
        }

        HeadOffice headOffice = currentManager.getHeadOffice();
        Site site = currentManager.getSite();

        if (site == null) {
            throw new BadRequestException("본사 또는 현장 정보가 배정되지 않은 관리자입니다.");
        }

        // 3. 같은 본사 + 같은 현장 + ROLE_MANAGER인 유저 조회
        List<User> managers = userRepository.findByHeadOfficeAndSiteAndRole(
                headOffice, site, Role.ROLE_MANAGER
        );

        // 4. DTO 변환 (자기 자신은 목록에서 뺄지 말지 결정. 보통 뺍니다.)
        return managers.stream()
                // .filter(user -> !user.getId().equals(currentManagerId)) // [선택] 나 자신은 제외하고 싶으면 주석 해제
                .map(ManagerListResponse::from)
                .collect(Collectors.toList());
    }
}
