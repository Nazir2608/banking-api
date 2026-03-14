package com.nazir.banking.transfer.controller;

import com.nazir.banking.common.dto.ApiResponse;
import com.nazir.banking.common.dto.PagedResponse;
import com.nazir.banking.transfer.dto.TransferRequest;
import com.nazir.banking.transfer.dto.TransferResponse;
import com.nazir.banking.transfer.service.TransferService;
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
@Tag(name = "Transfers")
@SecurityRequirement(name = "bearerAuth")
public class TransferController {

    private final TransferService transferService;

    @PostMapping("/v1/transfers")
    @Operation(summary = "Initiate a bank transfer")
    public ResponseEntity<ApiResponse<TransferResponse>> transfer(@AuthenticationPrincipal UserDetails userDetails,
                                                                   @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(ApiResponse.success(transferService.transfer(userDetails.getUsername(), request)));
    }

    @GetMapping("/v1/transfers")
    @Operation(summary = "Get my transfer history")
    public ResponseEntity<ApiResponse<PagedResponse<TransferResponse>>> getMyTransfers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(transferService.getMyTransfers(userDetails.getUsername(), pageable)));
    }

    @GetMapping("/v1/transfers/{reference}")
    @Operation(summary = "Get transfer by reference number")
    public ResponseEntity<ApiResponse<TransferResponse>> getByReference(@PathVariable String reference) {
        return ResponseEntity.ok(ApiResponse.success(transferService.getByReference(reference)));
    }
}
