package com.rikkeibank.service;

import com.rikkeibank.model.Account;
import com.rikkeibank.dto.AccountDtos.AccountResponse;
import com.rikkeibank.dto.AccountDtos.AdminAccountResponse;
import com.rikkeibank.dto.AccountDtos.UpdateAccountRequest;
import com.rikkeibank.dto.UserDtos.ChangePinRequest;
import com.rikkeibank.exception.BusinessException;
import com.rikkeibank.repository.AccountRepository;
import com.rikkeibank.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class AccountService {
    private final AccountRepository accounts;
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AccountService(AccountRepository accounts, UserRepository users, PasswordEncoder encoder) {
        this.accounts = accounts;
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> balances(String username) {
        log.debug("Loading balances for username={}", username);
        return accounts.findByUserUsername(username).stream().map(this::map).toList();
    }

    @Transactional
    public void changePin(Long accountId, String username, ChangePinRequest request) {
        Account account = accounts.findByIdAndUserUsername(accountId, username)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Account not found"));

        if (!encoder.matches(request.currentPin(), account.getTransactionPin())) {
            log.warn("Rejected PIN change for accountId={} username={}", accountId, username);
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Current PIN is incorrect");
        }

        account.setTransactionPin(encoder.encode(request.newPin()));
        log.info("Changed transaction PIN for accountId={} username={}", accountId, username);
    }

    Account requireOwned(Long id, String username) {
        return accounts.findByIdAndUserUsername(id, username)
            .orElseThrow(() -> new BusinessException(HttpStatus.FORBIDDEN, "Account is not owned by current user"));
    }

    @Transactional(readOnly = true)
    public Page<AdminAccountResponse> list(Pageable pageable) {
        log.debug("Listing accounts page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return accounts.findAll(pageable).map(this::adminMap);
    }

    @Transactional
    public AdminAccountResponse create(Long userId, String pin, String currency) {
        var user = users.findById(userId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));

        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber("RB" + System.currentTimeMillis());
        account.setTransactionPin(encoder.encode(pin));
        account.setCurrency(currency == null ? "VND" : currency);

        Account saved = accounts.save(account);
        log.info("Created account id={} for userId={}", saved.getId(), userId);
        return adminMap(saved);
    }

    @Transactional
    public AdminAccountResponse update(Long id, UpdateAccountRequest request) {
        Account account = accounts.findById(id)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Account not found"));

        if (request.active() != null) {
            account.setActive(request.active());
        }
        if (request.currency() != null) {
            account.setCurrency(request.currency());
        }

        log.info("Updated account id={} active={} currency={}", id, account.isActive(), account.getCurrency());
        return adminMap(account);
    }

    @Transactional
    public void delete(Long id) {
        accounts.findById(id).ifPresent(account -> {
            account.setActive(false);
            log.info("Deactivated account id={}", id);
        });
    }

    private AccountResponse map(Account account) {
        return new AccountResponse(
            account.getId(),
            account.getAccountNumber(),
            account.getBalance(),
            account.getCurrency(),
            account.isActive()
        );
    }

    private AdminAccountResponse adminMap(Account account) {
        return new AdminAccountResponse(
            account.getId(),
            account.getUser().getId(),
            account.getUser().getUsername(),
            account.getAccountNumber(),
            account.getBalance(),
            account.getCurrency(),
            account.isActive()
        );
    }
}
