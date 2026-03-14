package com.nazir.banking.account.service;

import com.nazir.banking.account.dto.AccountRequest;
import com.nazir.banking.account.entity.Account;
import com.nazir.banking.account.repository.AccountRepository;
import com.nazir.banking.common.exception.BadRequestException;
import com.nazir.banking.user.entity.User;
import com.nazir.banking.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private UserRepository userRepository;
    @Mock private AccountNumberGenerator accountNumberGenerator;

    @InjectMocks private AccountService accountService;

    @Test
    void openAccount_success() {
        User user = User.builder().id("u1").email("john@example.com").firstName("John")
                .lastName("Doe").role(User.Role.CUSTOMER).active(true).build();

        Account saved = Account.builder().id("a1").accountNumber("SB001")
                .accountType(Account.AccountType.SAVINGS).status(Account.AccountStatus.ACTIVE)
                .balance(BigDecimal.ZERO).user(user).build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(accountNumberGenerator.generate()).thenReturn("SB001");
        when(accountRepository.save(any())).thenReturn(saved);

        AccountRequest req = new AccountRequest();
        req.setAccountType(Account.AccountType.SAVINGS);

        var response = accountService.openAccount("john@example.com", req);
        assertThat(response.getAccountNumber()).isEqualTo("SB001");
        assertThat(response.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void closeAccount_withBalance_throws() {
        User user = User.builder().id("u1").email("john@example.com")
                .firstName("John").lastName("Doe").role(User.Role.CUSTOMER).active(true).build();

        Account account = Account.builder().id("a1").accountNumber("SB001")
                .status(Account.AccountStatus.ACTIVE).balance(new BigDecimal("500.00")).user(user).build();

        when(accountRepository.findById("a1")).thenReturn(Optional.of(account));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> accountService.closeAccount("a1", "john@example.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("zero balance");
    }
}
