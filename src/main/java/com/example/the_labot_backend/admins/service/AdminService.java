package com.example.the_labot_backend.admins.service;

import com.example.the_labot_backend.admins.dto.ManagerCreateRequest;
import com.example.the_labot_backend.authuser.entity.Role;
import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.sites.entity.Site;
import com.example.the_labot_backend.sites.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final SiteRepository siteRepository;
    private final PasswordEncoder passwordEncoder;
    
    // 현장 관리자 생성
    @Transactional
    public void createManager(Long siteId, ManagerCreateRequest request) {

        // 현장 존재 여부 확인
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new RuntimeException("현장을 찾을 수 없습니다."));

        // 전화번호 중복 검사
        if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new RuntimeException("이미 존재하는 전화번호입니다.");
        }

        // 4. 임시 비밀번호 생성
        String tempPw = "1234"; // 또는 랜덤 생성

        // 5. User 저장 (ROLE_MANAGER)
        User manager = User.builder()
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(tempPw))
                .name(request.getName())
                .role(Role.ROLE_MANAGER)
                .site(site)
                .build();

        userRepository.save(manager);

        // 나중에 SMS 보내기 가능
    }

}
