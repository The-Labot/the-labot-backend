package com.example.the_labot_backend.authuser.service;

import com.example.the_labot_backend.admins.entity.Admin;
import com.example.the_labot_backend.authuser.dto.AdminSignupRequest;
import com.example.the_labot_backend.authuser.dto.LoginRequest;
import com.example.the_labot_backend.authuser.dto.LoginResponse;
import com.example.the_labot_backend.authuser.entity.Role;
import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.global.config.JwtTokenProvider;
import com.example.the_labot_backend.global.exception.BadRequestException;
import com.example.the_labot_backend.global.exception.ConflictException;
import com.example.the_labot_backend.headoffice.repository.HeadOfficeRepository;
import com.example.the_labot_backend.sites.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SiteRepository siteRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final HeadOfficeRepository headOfficeRepository;

    // 로그인
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        
        // 전화번호로 User 찾기
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new BadCredentialsException("해당 전화번호가 존재하지 않습니다."));

        // **테스트용 임시 주석 처리
        // 비밀번호 조회
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {

            System.out.println("암호화된 비밀번호: " + passwordEncoder.encode(request.getPassword()));
            System.out.println("암호화된 비밀번호: " + user.getPassword());
            throw new BadCredentialsException("비밀번호가 올바르지 않습니다.");
        }

        // clientType 값 체크
        if (request.getClientType() == null) {
            throw new BadCredentialsException("clientType(APP/WEB)이 필요합니다.");
        }

        String type = request.getClientType().toUpperCase();
        Role role = user.getRole();

        // APP 본사관리자(Admin) 로그인 금지
        if (request.getClientType().equalsIgnoreCase("APP")
                && user.getRole() == Role.ROLE_ADMIN) {
            throw new BadCredentialsException("본사관리자는 앱에서 로그인할 수 없습니다.");
        }

        // WEB Admin 이외의 사용자 로그인 금지
        if (request.getClientType().equalsIgnoreCase("WEB")
                && user.getRole() != Role.ROLE_ADMIN) {
            throw new BadCredentialsException("현장관리자/근로자는 웹에서 로그인할 수 없습니다.");
        }

        String token = jwtTokenProvider.generateToken(user.getId(),user.getRole().name());

        return LoginResponse.builder()
                .token("Bearer " + token)
                .role(user.getRole().name())
                .userId(user.getId())
                .name(user.getName())
                .build();
    }

    // 본사관리자 회원가입
    @Transactional
    public void signupAdmin(AdminSignupRequest request) {

        if (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()) {
            throw new BadRequestException("전화번호는 필수 입력값입니다.");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("비밀번호는 필수 입력값입니다.");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("이름은 필수 입력값입니다.");
        }

        // 전화번호 중복 체크
        if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new ConflictException("이미 존재하는 전화번호입니다.");
        }

        // 패스워드 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .phoneNumber(request.getPhoneNumber())
                .password(encodedPassword)
                .name(request.getName())
                .role(Role.ROLE_ADMIN)   // 본사관리자 역할
                .build();

        Admin admin = Admin.builder()
                .email(request.getEmail())
                .address(request.getAddress())
                .user(user)
                .build();

        user.setAdmin(admin);

        userRepository.save(user);
    }
}
