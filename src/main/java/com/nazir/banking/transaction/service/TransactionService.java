package com.nazir.banking.transaction.service;

import com.nazir.banking.account.entity.Account;
import com.nazir.banking.account.repository.AccountRepository;
import com.nazir.banking.common.dto.PagedResponse;
import com.nazir.banking.common.exception.BadRequestException;
import com.nazir.banking.common.exception.InsufficientFundsException;
import com.nazir.banking.common.exception.ResourceNotFoundException;
import com.nazir.banking.transaction.dto.DepositWithdrawRequest;
import com.nazir.banking.transaction.dto.TransactionResponse;
import com.nazir.banking.transaction.entity.Transaction;
import com.nazir.banking.transaction.repository.TransactionRepository;
import com.nazir.banking.user.entity.User;
import com.nazir.banking.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional
    public TransactionResponse deposit(String email, DepositWithdrawRequest request) {
        Account account = getLockedAccount(request.getAccountId());
        validateOwnership(account, email);
        validateActive(account);

        BigDecimal before = account.getBalance();
        account.setBalance(before.add(request.getAmount()));
        accountRepository.save(account);

        return TransactionResponse.from(saveTransaction(
                account, Transaction.TransactionType.DEPOSIT,
                request.getAmount(), before, account.getBalance(), request.getDescription()));
    }

    @Transactional
    public TransactionResponse withdraw(String email, DepositWithdrawRequest request) {
        Account account = getLockedAccount(request.getAccountId());
        validateOwnership(account, email);
        validateActive(account);

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException();
        }

        BigDecimal before = account.getBalance();
        account.setBalance(before.subtract(request.getAmount()));
        accountRepository.save(account);

        return TransactionResponse.from(saveTransaction(
                account, Transaction.TransactionType.WITHDRAWAL,
                request.getAmount(), before, account.getBalance(), request.getDescription()));
    }

    public PagedResponse<TransactionResponse> getMyTransactions(String email, Pageable pageable) {
        User user = findUser(email);
        List<String> accountIds = accountRepository.findByUserId(user.getId())
                .stream().map(Account::getId).toList();
        return PagedResponse.of(transactionRepository.findByAccountIdIn(accountIds, pageable)
                .map(TransactionResponse::from));
    }

    public PagedResponse<TransactionResponse> getAccountTransactions(String accountId, String email, Pageable pageable) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> ResourceNotFoundException.of("Account", accountId));
        validateOwnership(account, email);
        return PagedResponse.of(transactionRepository.findByAccountId(accountId, pageable)
                .map(TransactionResponse::from));
    }

    public TransactionResponse getTransaction(String id, String email) {
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Transaction", id));
        validateOwnership(tx.getAccount(), email);
        return TransactionResponse.from(tx);
    }

    public Transaction saveTransaction(Account account, Transaction.TransactionType type,
                                       BigDecimal amount, BigDecimal before,
                                       BigDecimal after, String description) {
        Transaction tx = Transaction.builder()
                .account(account)
                .type(type)
                .amount(amount)
                .balanceBefore(before)
                .balanceAfter(after)
                .description(description)
                .build();
        return transactionRepository.save(tx);
    }

    private Account getLockedAccount(String accountId) {
        return accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> ResourceNotFoundException.of("Account", accountId));
    }

    private void validateActive(Account account) {
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new BadRequestException("Account is not active");
        }
    }

    private void validateOwnership(Account account, String email) {
        User user = findUser(email);
        if (user.getRole() == User.Role.ADMIN) return;
        if (!account.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied to this account");
        }
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> ResourceNotFoundException.of("User", email));
    }
}
