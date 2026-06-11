package com.rikkeibank.service;

import com.rikkeibank.model.Account;
import com.rikkeibank.model.BankTransaction;
import com.rikkeibank.model.TransactionStatus;
import com.rikkeibank.model.TransactionType;
import com.rikkeibank.dto.TransactionDtos.TransactionResponse;
import com.rikkeibank.dto.TransactionDtos.TransferRequest;
import com.rikkeibank.exception.BusinessException;
import com.rikkeibank.repository.AccountRepository;
import com.rikkeibank.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class TransferService {
    private final AccountRepository accounts;
    private final TransactionRepository transactions;
    private final PasswordEncoder encoder;

    public TransferService(
        AccountRepository accounts,
        TransactionRepository transactions,
        PasswordEncoder encoder
    ) {
        this.accounts = accounts;
        this.transactions = transactions;
        this.encoder = encoder;
    }

    @Transactional
    public TransactionResponse transfer(String username, TransferRequest request) {
        Account owned = accounts.findByIdAndUserUsername(request.fromAccountId(), username)
            .orElseThrow(() -> new BusinessException(HttpStatus.FORBIDDEN, "Source account is not owned by current user"));

        validateTransferOwnerState(owned, request.pin());

        Account from = accounts.findByIdForUpdate(request.fromAccountId()).orElseThrow();
        if (!from.isActive()) {
            throw new BusinessException(HttpStatus.CONFLICT, "Source account is inactive");
        }
        if (from.getBalance().compareTo(request.amount()) < 0) {
            log.warn("Rejected transfer for insufficient balance accountId={}", from.getId());
            throw new BusinessException(HttpStatus.CONFLICT, "Insufficient balance");
        }

        Account to = null;
        TransactionType type;
        if (request.toAccountId() != null) {
            to = resolveInternalDestination(request.toAccountId(), from.getId());
            type = TransactionType.INTERNAL;
        } else {
            validateInterbankBeneficiary(request);
            type = TransactionType.INTERBANK;
        }

        from.setBalance(from.getBalance().subtract(request.amount()));
        if (to != null) {
            to.setBalance(to.getBalance().add(request.amount()));
        }

        BankTransaction transaction = new BankTransaction();
        transaction.setTransactionCode("TX-" + UUID.randomUUID());
        transaction.setFromAccount(from);
        transaction.setToAccount(to);
        transaction.setBeneficiaryBank(request.beneficiaryBank());
        transaction.setBeneficiaryAccount(request.beneficiaryAccount());
        transaction.setAmount(request.amount());
        transaction.setDescription(request.description());
        transaction.setType(type);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCreatedAt(LocalDateTime.now());

        BankTransaction saved = transactions.save(transaction);
        log.info("Transfer completed code={} type={} amount={}", saved.getTransactionCode(), type, saved.getAmount());
        return map(saved, from.getId());
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> statement(
        String username,
        Long accountId,
        LocalDateTime fromDate,
        LocalDateTime toDate,
        Pageable pageable
    ) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "from must be before to");
        }

        accounts.findByIdAndUserUsername(accountId, username)
            .orElseThrow(() -> new BusinessException(HttpStatus.FORBIDDEN, "Account is not owned by current user"));

        log.debug("Loading statement accountId={} username={}", accountId, username);
        return transactions.statement(accountId, fromDate, toDate, pageable).map(t -> map(t, accountId));
    }

    private void validateTransferOwnerState(Account account, String pin) {
        if (!account.isActive()) {
            throw new BusinessException(HttpStatus.CONFLICT, "Source account is inactive");
        }
        if (!account.getUser().isKyc()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "KYC approval is required");
        }
        if (!encoder.matches(pin, account.getTransactionPin())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid transaction PIN");
        }
    }

    private Account resolveInternalDestination(Long toAccountId, Long fromAccountId) {
        if (toAccountId.equals(fromAccountId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Source and destination must differ");
        }

        Account to = accounts.findByIdForUpdate(toAccountId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Destination account not found"));

        if (!to.isActive()) {
            throw new BusinessException(HttpStatus.CONFLICT, "Destination account is inactive");
        }
        return to;
    }

    private void validateInterbankBeneficiary(TransferRequest request) {
        if (isBlank(request.beneficiaryBank()) || isBlank(request.beneficiaryAccount())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Interbank beneficiary is required");
        }
    }

    private TransactionResponse map(BankTransaction transaction, Long accountId) {
        Long toAccountId = transaction.getToAccount() == null ? null : transaction.getToAccount().getId();
        String direction = transaction.getFromAccount().getId().equals(accountId) ? "DEBIT" : "CREDIT";

        return new TransactionResponse(
            transaction.getTransactionCode(),
            transaction.getFromAccount().getId(),
            toAccountId,
            transaction.getAmount(),
            transaction.getDescription(),
            transaction.getType().name(),
            transaction.getStatus().name(),
            direction,
            transaction.getCreatedAt()
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
