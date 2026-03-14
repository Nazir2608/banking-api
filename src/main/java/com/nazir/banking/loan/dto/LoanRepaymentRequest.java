package com.nazir.banking.loan.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class LoanRepaymentRequest {

    @NotBlank(message = "Source account ID is required")
    private String accountId;

    @NotNull(message = "Repayment amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
}
