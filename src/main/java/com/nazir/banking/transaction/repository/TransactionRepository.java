package com.nazir.banking.transaction.repository;

import com.nazir.banking.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Page<Transaction> findByAccountId(String accountId, Pageable pageable);

    Page<Transaction> findByAccountIdIn(List<String> accountIds, Pageable pageable);

    Page<Transaction> findByAccountIdAndCreatedAtBetween(String accountId,
                                                          LocalDateTime from,
                                                          LocalDateTime to,
                                                          Pageable pageable);
}
