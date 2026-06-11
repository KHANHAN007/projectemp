package com.rikkeibank.service;

import com.rikkeibank.model.Account;
import com.rikkeibank.model.RoleName;
import com.rikkeibank.model.User;
import com.rikkeibank.dto.UserDtos.RegisterRequest;
import com.rikkeibank.dto.UserDtos.UpdateRequest;
import com.rikkeibank.dto.UserResponse;
import com.rikkeibank.exception.BusinessException;
import com.rikkeibank.repository.AccountRepository;
import com.rikkeibank.repository.RoleRepository;
import com.rikkeibank.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class UserService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final AccountRepository accounts;
    private final PasswordEncoder encoder;

    public UserService(
        UserRepository users,
        RoleRepository roles,
        AccountRepository accounts,
        PasswordEncoder encoder
    ) {
        this.users = users;
        this.roles = roles;
        this.accounts = accounts;
        this.encoder = encoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (users.existsByUsernameOrEmailOrPhoneNumber(
            request.username(),
            request.email(),
            request.phoneNumber()
        )) {
            log.warn("Rejected duplicate registration username={}", request.username());
            throw new BusinessException(HttpStatus.CONFLICT, "Username, email or phone already exists");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setPassword(encoder.encode(request.password()));
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        user.setRole(roles.findByName(RoleName.CUSTOMER).orElseThrow());
        users.save(user);

        createAccount(user, request.transactionPin(), "VND");
        log.info("Registered customer userId={} username={}", user.getId(), user.getUsername());
        return map(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> list(Pageable pageable) {
        log.debug("Listing users pageable={}", pageable);
        return users.findProjected(pageable);
    }

    @Transactional
    public UserResponse update(Long id, UpdateRequest request) {
        User user = users.findById(id)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User not found"));

        if (request.email() != null) {
            user.setEmail(request.email());
        }
        if (request.phoneNumber() != null) {
            user.setPhoneNumber(request.phoneNumber());
        }
        if (request.active() != null) {
            user.setActive(request.active());
        }
        if (request.role() != null) {
            user.setRole(roles.findByName(request.role()).orElseThrow());
        }

        log.info("Updated user id={} active={} role={}", id, user.isActive(), user.getRole().getName());
        return map(user);
    }

    @Transactional
    public void delete(Long id) {
        users.findById(id).ifPresent(user -> {
            user.setActive(false);
            log.info("Deactivated user id={}", id);
        });
    }

    @Transactional
    public Account createAccount(User user, String pin, String currency) {
        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber("RB" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 999));
        account.setTransactionPin(encoder.encode(pin));
        account.setCurrency(currency == null ? "VND" : currency);
        account.setBalance(BigDecimal.ZERO);

        Account saved = accounts.save(account);
        log.info("Opened default account id={} for userId={}", saved.getId(), user.getId());
        return saved;
    }

    private UserResponse map(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getRole().getName(),
            user.isActive(),
            user.isKyc(),
            user.getCreatedAt()
        );
    }
}
