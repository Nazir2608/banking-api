package com.nazir.banking.transfer.repository;

import com.nazir.banking.transfer.entity.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, String> {

    Optional<Transfer> findByReferenceNumber(String referenceNumber);

    @Query("SELECT t FROM Transfer t WHERE t.fromAccount.id IN :accountIds OR t.toAccount.id IN :accountIds")
    Page<Transfer> findByAccountIds(@Param("accountIds") List<String> accountIds, Pageable pageable);
}
