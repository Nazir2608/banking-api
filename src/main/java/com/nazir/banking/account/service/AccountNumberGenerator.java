package com.nazir.banking.account.service;

import com.nazir.banking.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class AccountNumberGenerator {

    private static final String PREFIX = "SB";
    private static final SecureRandom RANDOM = new SecureRandom();
    private final AccountRepository accountRepository;

    public String generate() {
        String number;
        do {
            long ts = System.currentTimeMillis() % 100_000_000L;
            int rand = RANDOM.nextInt(10000);
            number = String.format("%s%08d%04d", PREFIX, ts, rand);
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }
}
