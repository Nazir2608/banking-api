package com.nazir.banking.transfer.service;

import com.nazir.banking.account.entity.Account;
import com.nazir.banking.account.repository.AccountRepository;
import com.nazir.banking.common.dto.PagedResponse;
import com.nazir.banking.common.exception.BadRequestException;
import com.nazir.banking.common.exception.InsufficientFundsException;
import com.nazir.banking.common.exception.ResourceNotFoundException;
import com.nazir.banking.transaction.entity.Transaction;
import com.nazir.banking.transaction.service.TransactionService;
import com.nazir.banking.transfer.dto.TransferRequest;
import com.nazir.banking.transfer.dto.TransferResponse;
import com.nazir.banking.transfer.entity.Transfer;
import com.nazir.banking.transfer.repository.TransferRepository;
import com.nazir.banking.user.entity.User;
import com.nazir.banking.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    @Transactional
    public TransferResponse transfer(String email, TransferRequest request) {
        User user = findUser(email);

        Account fromAccount = accountRepository.findByIdForUpdate(request.getFromAccountId())
                .orElseThrow(() -> ResourceNotFoundException.of("Account", request.getFromAccountId()));

        Account toAccount = accountRepository.findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() -> ResourceNotFoundException.of("Account", request.getToAccountNumber()));

        boolean isAdmin = user.getRole() == User.Role.ADMIN;
        if (!isAdmin && !fromAccount.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not own this account");
        }

        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new BadRequestException("Cannot transfer to the same account");
        }

        if (fromAccount.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new BadRequestException("Source account is not active");
        }
        if (toAccount.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new BadRequestException("Destination account is not active");
        }

        BigDecimal amount = request.getAmount();
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }

        BigDecimal fromBefore = fromAccount.getBalance();
        BigDecimal toBefore   = toAccount.getBalance();

        fromAccount.setBalance(fromBefore.subtract(amount));
        toAccount.setBalance(toBefore.add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        transactionService.saveTransaction(fromAccount, Transaction.TransactionType.WITHDRAWAL,
                amount, fromBefore, fromAccount.getBalance(), "Transfer out: " + request.getDescription());
        transactionService.saveTransaction(toAccount, Transaction.TransactionType.DEPOSIT,
                amount, toBefore, toAccount.getBalance(), "Transfer in: " + request.getDescription());

        Transfer transfer = Transfer.builder()
                .referenceNumber(generateReference())
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(amount)
                .status(Transfer.TransferStatus.COMPLETED)
                .description(request.getDescription())
                .build();

        return TransferResponse.from(transferRepository.save(transfer));
    }

    public PagedResponse<TransferResponse> getMyTransfers(String email, Pageable pageable) {
        User user = findUser(email);
        List<String> accountIds = accountRepository.findByUserId(user.getId())
                .stream().map(Account::getId).toList();
        return PagedResponse.of(transferRepository.findByAccountIds(accountIds, pageable)
                .map(TransferResponse::from));
    }

    public TransferResponse getByReference(String reference) {
        return TransferResponse.from(transferRepository.findByReferenceNumber(reference)
                .orElseThrow(() -> ResourceNotFoundException.of("Transfer", reference)));
    }

    private String generateReference() {
        return "TXF" + Instant.now().toEpochMilli() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> ResourceNotFoundException.of("User", email));
    }
}
