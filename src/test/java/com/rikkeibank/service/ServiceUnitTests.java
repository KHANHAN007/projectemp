package com.rikkeibank.service;

import com.rikkeibank.model.Account;
import com.rikkeibank.model.Role;
import com.rikkeibank.model.RoleName;
import com.rikkeibank.model.User;
import com.rikkeibank.dto.TransactionDtos.TransferRequest;
import com.rikkeibank.dto.UserDtos.ChangePinRequest;
import com.rikkeibank.dto.UserDtos.RegisterRequest;
import com.rikkeibank.exception.BusinessException;
import com.rikkeibank.repository.AccountRepository;
import com.rikkeibank.repository.RoleRepository;
import com.rikkeibank.repository.TransactionRepository;
import com.rikkeibank.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceUnitTests {
    @Mock UserRepository users; @Mock RoleRepository roles; @Mock AccountRepository accounts;
    @Mock TransactionRepository transactions; @Mock PasswordEncoder encoder;

    @Test void registerRejectsDuplicateIdentity(){
        when(users.existsByUsernameOrEmailOrPhoneNumber(any(),any(),any())).thenReturn(true);
        var service=new UserService(users,roles,accounts,encoder);
        assertThrows(BusinessException.class,()->service.register(new RegisterRequest("user1","Password1","u@e.com","0901","123456")));
    }
    @Test void registerCreatesCustomerAndAccount(){
        Role role=new Role();role.setName(RoleName.CUSTOMER);when(roles.findByName(RoleName.CUSTOMER)).thenReturn(Optional.of(role));
        when(encoder.encode(any())).thenReturn("hash");when(users.save(any())).thenAnswer(i->i.getArgument(0));when(accounts.save(any())).thenAnswer(i->i.getArgument(0));
        var result=new UserService(users,roles,accounts,encoder).register(new RegisterRequest("user1","Password1","u@e.com","0901","123456"));
        assertEquals(RoleName.CUSTOMER,result.role());verify(accounts).save(any());
    }
    @Test void transferRejectsInsufficientBalance(){
        Account a=account(1L,"alice","100");when(accounts.findByIdAndUserUsername(1L,"alice")).thenReturn(Optional.of(a));
        when(encoder.matches("123456",a.getTransactionPin())).thenReturn(true);when(accounts.findByIdForUpdate(1L)).thenReturn(Optional.of(a));
        var service=new TransferService(accounts,transactions,encoder);
        assertThrows(BusinessException.class,()->service.transfer("alice",new TransferRequest(1L,2L,null,null,new BigDecimal("101"),"x","123456")));
    }
    @Test void transferRejectsUnownedAccount(){
        var service=new TransferService(accounts,transactions,encoder);
        assertThrows(BusinessException.class,()->service.transfer("alice",new TransferRequest(1L,2L,null,null,BigDecimal.ONE,"x","123456")));
    }
    @Test void changePinRejectsWrongCurrentPin(){
        Account a=account(1L,"alice","100");when(accounts.findByIdAndUserUsername(1L,"alice")).thenReturn(Optional.of(a));when(encoder.matches("bad","pin-hash")).thenReturn(false);
        assertThrows(BusinessException.class,()->new AccountService(accounts,users,encoder).changePin(1L,"alice",new ChangePinRequest("bad","654321")));
    }
    private Account account(Long id,String username,String balance){
        Role r=new Role();r.setName(RoleName.CUSTOMER);User u=new User();u.setUsername(username);u.setRole(r);u.setKyc(true);
        Account a=new Account();a.setId(id);a.setUser(u);a.setBalance(new BigDecimal(balance));a.setTransactionPin("pin-hash");return a;
    }
}
