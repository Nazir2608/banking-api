package com.nazir.banking.loan.controller;

import com.nazir.banking.common.dto.ApiResponse;
import com.nazir.banking.common.dto.PagedResponse;
import com.nazir.banking.loan.dto.LoanApplicationRequest;
import com.nazir.banking.loan.dto.LoanRepaymentRequest;
import com.nazir.banking.loan.dto.LoanResponse;
import com.nazir.banking.loan.entity.Loan;
import com.nazir.banking.loan.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Loans")
@SecurityRequirement(name = "bearerAuth")
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/v1/loans/apply")
    @Operation(summary = "Apply for a loan")
    public ResponseEntity<ApiResponse<LoanResponse>> applyForLoan(@AuthenticationPrincipal UserDetails userDetails,
                                                                    @Valid @RequestBody LoanApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(loanService.applyForLoan(userDetails.getUsername(), request)));
    }

    @GetMapping("/v1/loans")
    @Operation(summary = "Get my loans")
    public ResponseEntity<ApiResponse<PagedResponse<LoanResponse>>> getMyLoans(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(loanService.getMyLoans(userDetails.getUsername(), pageable)));
    }

    @GetMapping("/v1/loans/{id}")
    @Operation(summary = "Get loan by ID")
    public ResponseEntity<ApiResponse<LoanResponse>> getLoanById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.success(loanService.getLoanById(id)));
    }

    @PostMapping("/v1/loans/{id}/repay")
    @Operation(summary = "Make a loan repayment")
    public ResponseEntity<ApiResponse<LoanResponse>> repayLoan(@PathVariable String id,
                                                                @AuthenticationPrincipal UserDetails userDetails,
                                                                @Valid @RequestBody LoanRepaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(loanService.repayLoan(id, userDetails.getUsername(), request)));
    }

    @PutMapping("/v1/admin/loans/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve a loan application (admin)")
    public ResponseEntity<ApiResponse<LoanResponse>> approveLoan(@PathVariable String id,
                                                                   @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(loanService.approveLoan(id, userDetails.getUsername())));
    }

    @PutMapping("/v1/admin/loans/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject a loan application (admin)")
    public ResponseEntity<ApiResponse<LoanResponse>> rejectLoan(@PathVariable String id,
                                                                  @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(loanService.rejectLoan(id, userDetails.getUsername())));
    }

    @GetMapping("/v1/admin/loans")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all loans (admin)")
    public ResponseEntity<ApiResponse<PagedResponse<LoanResponse>>> getAllLoans(
            @RequestParam(required = false) Loan.LoanStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(loanService.getAllLoans(status, pageable)));
    }
}
