package com.rikkeibank.service;

import com.rikkeibank.domain.Account;
import com.rikkeibank.dto.AccountDtos.AccountResponse;
import com.rikkeibank.dto.AccountDtos.AdminAccountResponse;
import com.rikkeibank.dto.AccountDtos.UpdateAccountRequest;
import com.rikkeibank.dto.UserDtos.ChangePinRequest;
import com.rikkeibank.exception.BusinessException;
import com.rikkeibank.repository.AccountRepository;
import com.rikkeibank.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accounts; private final UserRepository users; private final PasswordEncoder encoder;
    public AccountService(AccountRepository accounts,UserRepository users,PasswordEncoder encoder){this.accounts=accounts;this.users=users;this.encoder=encoder;}
    public List<AccountResponse> balances(String username){return accounts.findByUserUsername(username).stream().map(this::map).toList();}
    @Transactional public void changePin(Long accountId,String username,ChangePinRequest req){
        Account a=accounts.findByIdAndUserUsername(accountId,username).orElseThrow(()->new BusinessException(HttpStatus.NOT_FOUND,"Account not found"));
        if(!encoder.matches(req.currentPin(),a.getTransactionPin()))throw new BusinessException(HttpStatus.UNAUTHORIZED,"Current PIN is incorrect");
        a.setTransactionPin(encoder.encode(req.newPin()));
    }
    Account requireOwned(Long id,String username){return accounts.findByIdAndUserUsername(id,username).orElseThrow(()->new BusinessException(HttpStatus.FORBIDDEN,"Account is not owned by current user"));}
    @Transactional(readOnly=true) public Page<AdminAccountResponse> list(Pageable p){return accounts.findAll(p).map(this::adminMap);}
    @Transactional public AdminAccountResponse create(Long userId,String pin,String currency){
        var u=users.findById(userId).orElseThrow(()->new BusinessException(HttpStatus.NOT_FOUND,"User not found"));
        Account a=new Account();a.setUser(u);a.setAccountNumber("RB"+System.currentTimeMillis());a.setTransactionPin(encoder.encode(pin));a.setCurrency(currency==null?"VND":currency);return adminMap(accounts.save(a));
    }
    @Transactional public AdminAccountResponse update(Long id,UpdateAccountRequest req){Account a=accounts.findById(id).orElseThrow(()->new BusinessException(HttpStatus.NOT_FOUND,"Account not found"));if(req.active()!=null)a.setActive(req.active());if(req.currency()!=null)a.setCurrency(req.currency());return adminMap(a);}
    @Transactional public void delete(Long id){accounts.findById(id).ifPresent(a->a.setActive(false));}
    private AccountResponse map(Account a){return new AccountResponse(a.getId(),a.getAccountNumber(),a.getBalance(),a.getCurrency(),a.isActive());}
    private AdminAccountResponse adminMap(Account a){return new AdminAccountResponse(a.getId(),a.getUser().getId(),a.getUser().getUsername(),a.getAccountNumber(),a.getBalance(),a.getCurrency(),a.isActive());}
}
