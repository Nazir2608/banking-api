package com.nazir.banking.loan.dto;

import com.nazir.banking.loan.entity.Loan;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class LoanResponse {
    private String id;
    private String accountId;
    private String accountNumber;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private Integer termMonths;
    private BigDecimal emiAmount;
    private BigDecimal outstanding;
    private BigDecimal totalRepaid;
    private String status;
    private String purpose;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;

    public static LoanResponse from(Loan loan) {
        return LoanResponse.builder()
                .id(loan.getId())
                .accountId(loan.getAccount().getId())
                .accountNumber(loan.getAccount().getAccountNumber())
                .amount(loan.getAmount())
                .interestRate(loan.getInterestRate())
                .termMonths(loan.getTermMonths())
                .emiAmount(loan.getEmiAmount())
                .outstanding(loan.getOutstanding())
                .totalRepaid(loan.getTotalRepaid())
                .status(loan.getStatus().name())
                .purpose(loan.getPurpose())
                .approvedBy(loan.getApprovedBy())
                .approvedAt(loan.getApprovedAt())
                .createdAt(loan.getCreatedAt())
                .build();
    }
}
