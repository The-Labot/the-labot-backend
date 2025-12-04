package com.example.the_labot_backend.authuser.service;

import com.example.the_labot_backend.authuser.entity.User;
import com.example.the_labot_backend.authuser.repository.UserRepository;
import com.example.the_labot_backend.global.exception.BadRequestException;
import com.example.the_labot_backend.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SmsService smsService;

    // 임시 패스워드 발급
    @Transactional
    public void resetPassword(String phoneNumber,String name) {

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new NotFoundException("해당 전화번호로 등록된 사용자가 없습니다."));

        if (!user.getName().equals(name)) {
            throw new BadRequestException("입력하신 이름이 일치하지 않습니다.");
        }

        // 임시 비밀번호 생성
        String tempPassword = "1234";
    //    String tempPassword = generateTempPassword();

        // 비밀번호 변경
        user.setPassword(passwordEncoder.encode(tempPassword));

        // 암호화 후 저장
        String encoded = passwordEncoder.encode(tempPassword);
        user.setPassword(encoded);

        // UPDATE 반영 (필수!! — 이게 빠지면 저장 X)
        userRepository.save(user);

        System.out.println(tempPassword);

        // 테스트용 임시 SMS 전송 주석
        // SMS 전송
        smsService.sendSms(
                phoneNumber,
                "[THE LABOT] 임시 비밀번호는 [" + tempPassword + "] 입니다.\n로그인 후 비밀번호를 변경하세요."
        );
    }

    // 임시 비밀번호 생성
    private String generateTempPassword() {
        int length = 8;
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789"; // 사용할 문자 정의

        StringBuilder sb = new StringBuilder();
        Random random = new Random(); 

        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length()))); //랜덤으로 문자 8자리 붙여주기
        }

        return sb.toString();
    }

    // 비밀번호 변경
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        // 기존 비밀번호가 맞는지 확인
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("기존 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 암호화 후 저장
        user.setPassword(passwordEncoder.encode(newPassword));
    }
}
