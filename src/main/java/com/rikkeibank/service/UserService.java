package com.rikkeibank.service;

import com.rikkeibank.domain.Account;
import com.rikkeibank.domain.RoleName;
import com.rikkeibank.domain.User;
import com.rikkeibank.dto.UserDtos.RegisterRequest;
import com.rikkeibank.dto.UserDtos.UpdateRequest;
import com.rikkeibank.dto.UserResponse;
import com.rikkeibank.exception.BusinessException;
import com.rikkeibank.repository.AccountRepository;
import com.rikkeibank.repository.RoleRepository;
import com.rikkeibank.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class UserService {
    private final UserRepository users; private final RoleRepository roles; private final AccountRepository accounts; private final PasswordEncoder encoder;
    public UserService(UserRepository users, RoleRepository roles, AccountRepository accounts, PasswordEncoder encoder) {
        this.users=users;this.roles=roles;this.accounts=accounts;this.encoder=encoder;
    }
    @Transactional public UserResponse register(RegisterRequest req) {
        if(users.existsByUsernameOrEmailOrPhoneNumber(req.username(),req.email(),req.phoneNumber()))
            throw new BusinessException(HttpStatus.CONFLICT,"Username, email or phone already exists");
        User u=new User();u.setUsername(req.username());u.setPassword(encoder.encode(req.password()));u.setEmail(req.email());
        u.setPhoneNumber(req.phoneNumber());u.setRole(roles.findByName(RoleName.CUSTOMER).orElseThrow());users.save(u);
        createAccount(u,req.transactionPin(),"VND");
        return map(u);
    }
    public Page<UserResponse> list(Pageable pageable) { return users.findProjected(pageable); }
    @Transactional public UserResponse update(Long id, UpdateRequest req) {
        User u=users.findById(id).orElseThrow(()->new BusinessException(HttpStatus.NOT_FOUND,"User not found"));
        if(req.email()!=null)u.setEmail(req.email());if(req.phoneNumber()!=null)u.setPhoneNumber(req.phoneNumber());
        if(req.active()!=null)u.setActive(req.active());if(req.role()!=null)u.setRole(roles.findByName(req.role()).orElseThrow());
        return map(u);
    }
    @Transactional public void delete(Long id) { users.findById(id).ifPresent(u->u.setActive(false)); }
    @Transactional public Account createAccount(User user,String pin,String currency) {
        Account a=new Account();a.setUser(user);a.setAccountNumber("RB"+System.currentTimeMillis()+ThreadLocalRandom.current().nextInt(100,999));
        a.setTransactionPin(encoder.encode(pin));a.setCurrency(currency==null?"VND":currency);a.setBalance(BigDecimal.ZERO);return accounts.save(a);
    }
    private UserResponse map(User u){return new UserResponse(u.getId(),u.getUsername(),u.getEmail(),u.getPhoneNumber(),u.getRole().getName(),u.isActive(),u.isKyc(),u.getCreatedAt());}
}
