package com.example.the_labot_backend.sites.entity.embeddable;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@Builder              // <--- 이 어노테이션이 있어야 .builder()를 쓸 수 있습니다.
@AllArgsConstructor   // <--- 빌더 패턴은 전체 생성자가 필요합니다.
@NoArgsConstructor    // <--- JPA(@Embeddable)는 기본 생성자가 필요합니다.
public class PayrollBankAccount {

    private String bankName; // 은행명
    private String accountNumber; // 계좌번호
    private String accountHolder; // 예금주명
    private String informPhoneNumber; // 통보 휴대폰 번호
}