package com.example.the_labot_backend.sites.dto;

import com.example.the_labot_backend.sites.entity.embeddable.BankAccount;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BankAccountResponse {
    private String bankName;
    private String accountNumber;
    private String accountHolder;

    public static BankAccountResponse from(BankAccount account) {
        if (account == null) return null;
        return BankAccountResponse.builder()
                .bankName(account.getBankName())
                .accountNumber(account.getAccountNumber())
                .accountHolder(account.getAccountHolder())
                .build();
    }
}