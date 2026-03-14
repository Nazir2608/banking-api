package com.nazir.banking.report.controller;

import com.nazir.banking.account.entity.Account;
import com.nazir.banking.account.service.AccountService;
import com.nazir.banking.common.dto.ApiResponse;
import com.nazir.banking.common.dto.PagedResponse;
import com.nazir.banking.transaction.dto.TransactionResponse;
import com.nazir.banking.transaction.repository.TransactionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reports")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    @GetMapping("/v1/reports/statement")
    @Operation(summary = "Get account statement for a date range")
    public ResponseEntity<ApiResponse<PagedResponse<TransactionResponse>>> getStatement(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        accountService.getAccountById(accountId, userDetails.getUsername());

        LocalDateTime from = startDate.atStartOfDay();
        LocalDateTime to   = endDate.atTime(23, 59, 59);

        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var result = transactionRepository
                .findByAccountIdAndCreatedAtBetween(accountId, from, to, pageable)
                .map(TransactionResponse::from);

        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(result)));
    }

    @GetMapping("/v1/admin/reports/summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get platform summary (admin)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalTransactions", transactionRepository.count());
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
}
