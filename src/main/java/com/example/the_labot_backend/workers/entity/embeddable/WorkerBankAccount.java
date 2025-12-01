package com.example.the_labot_backend.workers.entity.embeddable;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable // [★] 다른 엔티티에 내장될 수 있음을 표시
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerBankAccount {
    private String bankName;      // 은행명
    private String accountNumber; // 계좌번호
    private String accountHolder; // 예금주
}
