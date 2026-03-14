package com.nazir.banking.account.dto;

import com.nazir.banking.account.entity.Account;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountRequest {

    @NotNull(message = "Account type is required")
    private Account.AccountType accountType;
}
