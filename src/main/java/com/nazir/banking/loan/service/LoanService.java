package com.nazir.banking.loan.service;

import com.nazir.banking.account.entity.Account;
import com.nazir.banking.account.repository.AccountRepository;
import com.nazir.banking.common.dto.PagedResponse;
import com.nazir.banking.common.exception.BadRequestException;
import com.nazir.banking.common.exception.InsufficientFundsException;
import com.nazir.banking.common.exception.ResourceNotFoundException;
import com.nazir.banking.loan.dto.LoanApplicationRequest;
import com.nazir.banking.loan.dto.LoanRepaymentRequest;
import com.nazir.banking.loan.dto.LoanResponse;
import com.nazir.banking.loan.entity.Loan;
import com.nazir.banking.loan.repository.LoanRepository;
import com.nazir.banking.transaction.entity.Transaction;
import com.nazir.banking.transaction.service.TransactionService;
import com.nazir.banking.user.entity.User;
import com.nazir.banking.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    @Transactional
    public LoanResponse applyForLoan(String email, LoanApplicationRequest request) {
        User user = findUser(email);
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> ResourceNotFoundException.of("Account", request.getAccountId()));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Account does not belong to you");
        }
        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new BadRequestException("Account is not active");
        }

        BigDecimal emi = calculateEmi(request.getAmount(), request.getInterestRate(), request.getTermMonths());
        BigDecimal totalPayable = emi.multiply(BigDecimal.valueOf(request.getTermMonths()));

        Loan loan = Loan.builder()
                .user(user)
                .account(account)
                .amount(request.getAmount())
                .interestRate(request.getInterestRate())
                .termMonths(request.getTermMonths())
                .emiAmount(emi)
                .outstanding(totalPayable)
                .purpose(request.getPurpose())
                .build();

        return LoanResponse.from(loanRepository.save(loan));
    }

    public PagedResponse<LoanResponse> getMyLoans(String email, Pageable pageable) {
        User user = findUser(email);
        return PagedResponse.of(loanRepository.findByUserId(user.getId(), pageable).map(LoanResponse::from));
    }

    public LoanResponse getLoanById(String id) {
        return LoanResponse.from(findLoan(id));
    }

    @Transactional
    public LoanResponse approveLoan(String id, String approverEmail) {
        Loan loan = findLoan(id);
        if (loan.getStatus() != Loan.LoanStatus.PENDING) {
            throw new BadRequestException("Only pending loans can be approved");
        }

        Account account = accountRepository.findByIdForUpdate(loan.getAccount().getId()).orElseThrow();

        BigDecimal before = account.getBalance();
        account.setBalance(before.add(loan.getAmount()));
        accountRepository.save(account);

        transactionService.saveTransaction(account, Transaction.TransactionType.DEPOSIT,
                loan.getAmount(), before, account.getBalance(), "Loan disbursement: " + loan.getId());

        loan.setStatus(Loan.LoanStatus.ACTIVE);
        loan.setApprovedBy(approverEmail);
        loan.setApprovedAt(LocalDateTime.now());

        return LoanResponse.from(loanRepository.save(loan));
    }

    @Transactional
    public LoanResponse rejectLoan(String id, String approverEmail) {
        Loan loan = findLoan(id);
        if (loan.getStatus() != Loan.LoanStatus.PENDING) {
            throw new BadRequestException("Only pending loans can be rejected");
        }
        loan.setStatus(Loan.LoanStatus.REJECTED);
        loan.setApprovedBy(approverEmail);
        loan.setApprovedAt(LocalDateTime.now());
        return LoanResponse.from(loanRepository.save(loan));
    }

    @Transactional
    public LoanResponse repayLoan(String id, String email, LoanRepaymentRequest request) {
        Loan loan = findLoan(id);
        User user = findUser(email);

        if (!loan.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("This loan does not belong to you");
        }
        if (loan.getStatus() != Loan.LoanStatus.ACTIVE) {
            throw new BadRequestException("Only active loans can be repaid");
        }

        Account sourceAccount = accountRepository.findByIdForUpdate(request.getAccountId())
                .orElseThrow(() -> ResourceNotFoundException.of("Account", request.getAccountId()));

        if (!sourceAccount.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Account does not belong to you");
        }
        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException();
        }

        BigDecimal repayment = request.getAmount().min(loan.getOutstanding());

        BigDecimal before = sourceAccount.getBalance();
        sourceAccount.setBalance(before.subtract(repayment));
        accountRepository.save(sourceAccount);

        transactionService.saveTransaction(sourceAccount, Transaction.TransactionType.WITHDRAWAL,
                repayment, before, sourceAccount.getBalance(), "Loan repayment: " + loan.getId());

        loan.setTotalRepaid(loan.getTotalRepaid().add(repayment));
        loan.setOutstanding(loan.getOutstanding().subtract(repayment));

        if (loan.getOutstanding().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setOutstanding(BigDecimal.ZERO);
            loan.setStatus(Loan.LoanStatus.CLOSED);
        }

        return LoanResponse.from(loanRepository.save(loan));
    }

    public PagedResponse<LoanResponse> getAllLoans(Loan.LoanStatus status, Pageable pageable) {
        if (status != null) {
            return PagedResponse.of(loanRepository.findByStatus(status, pageable).map(LoanResponse::from));
        }
        return PagedResponse.of(loanRepository.findAll(pageable).map(LoanResponse::from));
    }

    private BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRate, int termMonths) {
        if (annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(termMonths), 4, RoundingMode.HALF_UP);
        }
        double r = annualRate.doubleValue() / 12.0 / 100.0;
        double p = principal.doubleValue();
        double emi = p * r * Math.pow(1 + r, termMonths) / (Math.pow(1 + r, termMonths) - 1);
        return BigDecimal.valueOf(emi).setScale(4, RoundingMode.HALF_UP);
    }

    private Loan findLoan(String id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Loan", id));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> ResourceNotFoundException.of("User", email));
    }
}
