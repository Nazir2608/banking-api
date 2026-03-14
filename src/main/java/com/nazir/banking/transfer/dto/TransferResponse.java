package com.nazir.banking.transfer.dto;

import com.nazir.banking.transfer.entity.Transfer;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class TransferResponse {
    private String id;
    private String referenceNumber;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private String status;
    private String description;
    private LocalDateTime createdAt;

    public static TransferResponse from(Transfer transfer) {
        return TransferResponse.builder()
                .id(transfer.getId())
                .referenceNumber(transfer.getReferenceNumber())
                .fromAccountNumber(transfer.getFromAccount().getAccountNumber())
                .toAccountNumber(transfer.getToAccount().getAccountNumber())
                .amount(transfer.getAmount())
                .status(transfer.getStatus().name())
                .description(transfer.getDescription())
                .createdAt(transfer.getCreatedAt())
                .build();
    }
}
