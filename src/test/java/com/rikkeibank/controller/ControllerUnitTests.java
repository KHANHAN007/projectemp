package com.rikkeibank.controller;

import com.rikkeibank.dto.AccountDtos.AccountResponse;
import com.rikkeibank.dto.AuthDtos.LoginRequest;
import com.rikkeibank.dto.AuthDtos.RefreshRequest;
import com.rikkeibank.dto.AuthDtos.TokenResponse;
import com.rikkeibank.dto.UserDtos.RegisterRequest;
import com.rikkeibank.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ControllerUnitTests {
    @Test void loginReturnsTokenEnvelope(){
        AuthService auth=mock(AuthService.class);PasswordResetService resets=mock(PasswordResetService.class);
        when(auth.login(any())).thenReturn(new TokenResponse("a","r","Bearer",300));
        assertTrue(new AuthController(auth,resets).login(new LoginRequest("u","p")).success());
    }
    @Test void refreshReturnsRotatedToken(){
        AuthService auth=mock(AuthService.class);when(auth.refresh("r")).thenReturn(new TokenResponse("a2","r2","Bearer",300));
        assertEquals("r2",new AuthController(auth,mock(PasswordResetService.class)).refresh(new RefreshRequest("r")).data().refreshToken());
    }
    @Test void registrationReturnsCreated(){
        UserService service=mock(UserService.class);var req=new RegisterRequest("user1","Password1","u@e.com","0901","123456");
        assertEquals(201,new RegistrationController(service).register(req).getStatusCode().value());
    }
    @Test void balancesReturnsOwnedAccounts(){
        AccountService accounts=mock(AccountService.class);when(accounts.balances("alice")).thenReturn(List.of(new AccountResponse(1L,"RB1",BigDecimal.TEN,"VND",true)));
        var controller=new CustomerController(accounts,mock(TransferService.class));
        assertEquals(1,controller.balances(new UsernamePasswordAuthenticationToken("alice","")).data().size());
    }
    @Test void adminListReturnsPage(){
        UserService service=mock(UserService.class);when(service.list(any())).thenReturn(new PageImpl<>(List.of()));
        assertTrue(new AdminUserController(service).list(org.springframework.data.domain.Pageable.unpaged()).success());
    }
}
