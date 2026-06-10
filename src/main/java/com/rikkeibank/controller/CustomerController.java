package com.rikkeibank.controller;

import com.rikkeibank.dto.AccountDtos.AccountResponse;
import com.rikkeibank.dto.ApiResponse;
import com.rikkeibank.dto.TransactionDtos.TransactionResponse;
import com.rikkeibank.dto.TransactionDtos.TransferRequest;
import com.rikkeibank.dto.UserDtos.ChangePinRequest;
import com.rikkeibank.service.AccountService;
import com.rikkeibank.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customer")
public class CustomerController {
    private final AccountService accounts;private final TransferService transfers;
    public CustomerController(AccountService accounts,TransferService transfers){this.accounts=accounts;this.transfers=transfers;}
    @GetMapping("/accounts/balances") public ApiResponse<List<AccountResponse>> balances(Authentication a){return ApiResponse.ok("Balances retrieved",accounts.balances(a.getName()));}
    @PutMapping("/accounts/{id}/pin") public ApiResponse<Void> pin(@PathVariable Long id,@Valid @RequestBody ChangePinRequest req,Authentication a){accounts.changePin(id,a.getName(),req);return ApiResponse.ok("PIN changed",null);}
    @PostMapping("/transactions/transfer") public ApiResponse<TransactionResponse> transfer(@Valid @RequestBody TransferRequest req,Authentication a){return ApiResponse.ok("Transfer successful",transfers.transfer(a.getName(),req));}
    @GetMapping("/accounts/{id}/transactions") public ApiResponse<Page<TransactionResponse>> statement(@PathVariable Long id,
        @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
        @PageableDefault(size=20,sort="createdAt",direction=Sort.Direction.DESC)Pageable p,Authentication a){
        return ApiResponse.ok("Statement retrieved",transfers.statement(a.getName(),id,from,to,p));
    }
}
