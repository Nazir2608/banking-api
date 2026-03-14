package com.nazir.banking.account.controller;

import com.nazir.banking.account.dto.AccountRequest;
import com.nazir.banking.account.dto.AccountResponse;
import com.nazir.banking.account.entity.Account;
import com.nazir.banking.account.service.AccountService;
import com.nazir.banking.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Accounts")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/v1/accounts")
    @Operation(summary = "Open a new bank account")
    public ResponseEntity<ApiResponse<AccountResponse>> openAccount(@AuthenticationPrincipal UserDetails userDetails,
                                                                     @Valid @RequestBody AccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(accountService.openAccount(userDetails.getUsername(), request)));
    }

    @GetMapping("/v1/accounts")
    @Operation(summary = "Get all my accounts")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getMyAccounts(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getMyAccounts(userDetails.getUsername())));
    }

    @GetMapping("/v1/accounts/{id}")
    @Operation(summary = "Get account by ID")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(@PathVariable String id,
                                                                        @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getAccountById(id, userDetails.getUsername())));
    }

    @GetMapping("/v1/accounts/{id}/balance")
    @Operation(summary = "Get account balance")
    public ResponseEntity<ApiResponse<AccountResponse>> getBalance(@PathVariable String id,
                                                                    @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getBalance(id, userDetails.getUsername())));
    }

    @PutMapping("/v1/admin/accounts/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update account status (admin)")
    public ResponseEntity<ApiResponse<AccountResponse>> updateStatus(@PathVariable String id,
                                                                      @RequestParam Account.AccountStatus status) {
        return ResponseEntity.ok(ApiResponse.success(accountService.updateAccountStatus(id, status)));
    }

    @DeleteMapping("/v1/accounts/{id}")
    @Operation(summary = "Close account")
    public ResponseEntity<ApiResponse<Void>> closeAccount(@PathVariable String id,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        accountService.closeAccount(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Account closed successfully"));
    }
}
