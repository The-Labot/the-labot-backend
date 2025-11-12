package com.example.the_labot_backend.auth;


import com.example.the_labot_backend.enums.Role;
import com.example.the_labot_backend.global.config.JwtTokenProvider;
import com.example.the_labot_backend.auth.dto.SignupRequest;
import com.example.the_labot_backend.sites.Site;
import com.example.the_labot_backend.sites.SiteRepository;
import com.example.the_labot_backend.users.User;
import com.example.the_labot_backend.users.UserRepository;
import com.example.the_labot_backend.users.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SiteRepository siteRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    // 로그인
    public String login(LoginRequest request) {
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new RuntimeException("해당 전화번호가 존재하지 않습니다."));

        // 테스트용 비밀번호 조회 하지 않도록 설정
//        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
//
//            System.out.println("암호화된 비밀번호: " + passwordEncoder.encode(request.getPassword()));
//            System.out.println("암호화된 비밀번호: " + user.getPassword());
//            throw new RuntimeException("비밀번호가 올바르지 않습니다.");
//        }

        return jwtTokenProvider.generateToken(user.getId(),user.getRole().name());
    }

    // 임시 회원가입
    public void signup(SignupRequest request) {
        // 중복 전화번호 검사
        if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new RuntimeException("이미 존재하는 전화번호입니다.");
        }

        // 현장Id로 현장 찾기
        Site site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 현장입니다."));


        // 비밀번호 암호화 후 저장
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
//        // 테스트용도 비밀번호 해쉬하지 않고 저장. 추후 개발 종료시 삭제
//        String encodedPassword = request.getPassword();
        
        User user = User.builder()
                .phoneNumber(request.getPhoneNumber())
                .password(encodedPassword)
                .name(request.getName())
                .site(site)
                .role(request.getRole() != null ? request.getRole() : Role.ROLE_WORKER)
                .build();

        userRepository.save(user);
        System.out.println("[회원가입 완료] 저장된 비밀번호 = " + encodedPassword);
    }

}
