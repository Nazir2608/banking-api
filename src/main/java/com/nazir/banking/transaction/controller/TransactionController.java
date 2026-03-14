package com.nazir.banking.transaction.controller;

import com.nazir.banking.common.dto.ApiResponse;
import com.nazir.banking.common.dto.PagedResponse;
import com.nazir.banking.transaction.dto.DepositWithdrawRequest;
import com.nazir.banking.transaction.dto.TransactionResponse;
import com.nazir.banking.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Transactions")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/v1/transactions/deposit")
    @Operation(summary = "Deposit funds")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(@AuthenticationPrincipal UserDetails userDetails,
                                                                     @Valid @RequestBody DepositWithdrawRequest request) {
        return ResponseEntity.ok(ApiResponse.success(transactionService.deposit(userDetails.getUsername(), request)));
    }

    @PostMapping("/v1/transactions/withdraw")
    @Operation(summary = "Withdraw funds")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(@AuthenticationPrincipal UserDetails userDetails,
                                                                      @Valid @RequestBody DepositWithdrawRequest request) {
        return ResponseEntity.ok(ApiResponse.success(transactionService.withdraw(userDetails.getUsername(), request)));
    }

    @GetMapping("/v1/transactions")
    @Operation(summary = "Get all my transactions")
    public ResponseEntity<ApiResponse<PagedResponse<TransactionResponse>>> getMyTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(transactionService.getMyTransactions(userDetails.getUsername(), pageable)));
    }

    @GetMapping("/v1/transactions/{id}")
    @Operation(summary = "Get transaction by ID")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(@PathVariable String id,
                                                                            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(transactionService.getTransaction(id, userDetails.getUsername())));
    }

    @GetMapping("/v1/accounts/{accountId}/transactions")
    @Operation(summary = "Get transactions for a specific account")
    public ResponseEntity<ApiResponse<PagedResponse<TransactionResponse>>> getAccountTransactions(
            @PathVariable String accountId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(
                transactionService.getAccountTransactions(accountId, userDetails.getUsername(), pageable)));
    }
}
