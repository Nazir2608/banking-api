package com.nazir.banking.account.dto;

import com.nazir.banking.account.entity.Account;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AccountResponse {
    private String id;
    private String accountNumber;
    private String accountType;
    private String status;
    private BigDecimal balance;
    private String ownerName;
    private LocalDateTime createdAt;

    public static AccountResponse from(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType().name())
                .status(account.getStatus().name())
                .balance(account.getBalance())
                .ownerName(account.getUser().getFullName())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
