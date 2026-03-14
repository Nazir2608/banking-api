package com.nazir.banking.account.service;

import com.nazir.banking.account.dto.AccountRequest;
import com.nazir.banking.account.dto.AccountResponse;
import com.nazir.banking.account.entity.Account;
import com.nazir.banking.account.repository.AccountRepository;
import com.nazir.banking.common.exception.BadRequestException;
import com.nazir.banking.common.exception.ResourceNotFoundException;
import com.nazir.banking.user.entity.User;
import com.nazir.banking.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountNumberGenerator accountNumberGenerator;

    @Transactional
    public AccountResponse openAccount(String email, AccountRequest request) {
        User user = findUserByEmail(email);

        Account account = Account.builder()
                .accountNumber(accountNumberGenerator.generate())
                .accountType(request.getAccountType())
                .user(user)
                .build();

        return AccountResponse.from(accountRepository.save(account));
    }

    public List<AccountResponse> getMyAccounts(String email) {
        User user = findUserByEmail(email);
        return accountRepository.findByUserId(user.getId())
                .stream().map(AccountResponse::from).toList();
    }

    public AccountResponse getAccountById(String id, String email) {
        Account account = findAccount(id);
        ensureOwnerOrAdmin(account, email);
        return AccountResponse.from(account);
    }

    public AccountResponse getBalance(String id, String email) {
        Account account = findAccount(id);
        ensureOwnerOrAdmin(account, email);
        return AccountResponse.from(account);
    }

    @Transactional
    public AccountResponse updateAccountStatus(String id, Account.AccountStatus status) {
        Account account = findAccount(id);
        if (account.getStatus() == Account.AccountStatus.CLOSED) {
            throw new BadRequestException("Closed accounts cannot be modified");
        }
        account.setStatus(status);
        return AccountResponse.from(accountRepository.save(account));
    }

    @Transactional
    public void closeAccount(String id, String email) {
        Account account = findAccount(id);
        ensureOwnerOrAdmin(account, email);

        if (account.getStatus() == Account.AccountStatus.CLOSED) {
            throw new BadRequestException("Account is already closed");
        }
        if (account.getBalance().signum() != 0) {
            throw new BadRequestException("Account must have zero balance before closing");
        }

        account.setStatus(Account.AccountStatus.CLOSED);
        accountRepository.save(account);
    }

    public Account findAccount(String id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Account", id));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> ResourceNotFoundException.of("User", email));
    }

    private void ensureOwnerOrAdmin(Account account, String email) {
        User user = findUserByEmail(email);
        boolean isAdmin = user.getRole() == User.Role.ADMIN;
        if (!isAdmin && !account.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Access denied to this account");
        }
    }
}
