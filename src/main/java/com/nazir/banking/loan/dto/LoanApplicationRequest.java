package com.nazir.banking.loan.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class LoanApplicationRequest {

    @NotBlank(message = "Account ID is required for loan disbursement")
    private String accountId;

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum loan amount is 1000")
    @DecimalMax(value = "10000000.00", message = "Maximum loan amount is 10,000,000")
    private BigDecimal amount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.01")
    @DecimalMax(value = "50.00")
    private BigDecimal interestRate;

    @NotNull(message = "Loan term is required")
    @Min(value = 1, message = "Minimum term is 1 month")
    @Max(value = 360, message = "Maximum term is 360 months")
    private Integer termMonths;

    @Size(max = 500)
    private String purpose;
}
