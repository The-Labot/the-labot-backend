package com.example.the_labot_backend.sites.dto;

import com.example.the_labot_backend.sites.entity.embeddable.PayrollBankAccount;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BankAccountResponse {
    private String bankName;
    private String accountNumber;
    private String accountHolder;
    private String informPhoneNumber;

    public static BankAccountResponse from(PayrollBankAccount account) {
        if (account == null) return null;
        return BankAccountResponse.builder()
                .bankName(account.getBankName())
                .accountNumber(account.getAccountNumber())
                .accountHolder(account.getAccountHolder())
                .informPhoneNumber(account.getInformPhoneNumber())
                .build();
    }
}