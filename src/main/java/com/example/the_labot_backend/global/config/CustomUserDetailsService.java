package com.example.the_labot_backend.global.config;

import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// 사용자 정의 서비스
// Spring Security가 로그인 과정에서 자동으로 호출해주는 클래스
// 사용자가 로그인할 때 입력한 정보를 바탕으로 DB에서 유저 정보를 찾아 UserDetails 객체로 바꿔 반환
@Service // 서비스 빈으로 등록, Security가 내부적으로 UserDetailsService 타입을 찾을 때 자동으로 인식함.
@RequiredArgsConstructor // final로 선언된 필드를 자동으로 생성자 주입해줌.
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    // loadUserByUsername의 메서드 시그니처는 반드시 String이어야 한다.
    // 이 메서드를 리플렉션(reflection)으로 호출하기 때문이다.
    // UserDetailService는 인증 과정에서 자동으로 호출되는 핵심 인터페이스
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        Long id = Long.parseLong(userId);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다(CustomUserDetailsSerivce클래스): " + id));

        // Spring Security는 내부적으로 로그인 정보를 UserDetails 타입으로 관리함.
        // Spring이 제공하는 기본 구현체 org.springframework.security.core.userdetails.User로 바꿔준다.
        // UserDetails 타입 객체를 통해 비밀번호 비교, 권한체크 등을 수행한다.
        return new org.springframework.security.core.userdetails.User(
                String.valueOf(user.getId()),
                user.getPassword(),

                // 사용자의 권한을 문자열에서 Security의 권한 객체로 변환함.
                AuthorityUtils.createAuthorityList(user.getRole().name())
        );
    }
}
