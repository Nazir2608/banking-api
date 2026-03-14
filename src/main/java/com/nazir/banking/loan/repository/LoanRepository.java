package com.nazir.banking.loan.repository;

import com.nazir.banking.loan.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, String> {

    Page<Loan> findByUserId(String userId, Pageable pageable);

    Page<Loan> findByStatus(Loan.LoanStatus status, Pageable pageable);
}
