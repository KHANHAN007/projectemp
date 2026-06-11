package com.rikkeibank.controller;

import com.rikkeibank.dto.AccountDtos.AccountResponse;
import com.rikkeibank.dto.ApiResponse;
import com.rikkeibank.dto.TransactionDtos.TransactionResponse;
import com.rikkeibank.dto.TransactionDtos.TransferRequest;
import com.rikkeibank.dto.UserDtos.ChangePinRequest;
import com.rikkeibank.service.AccountService;
import com.rikkeibank.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customer")
@Tag(name = "FR-06/07/08/10 Customer Banking", description = "Balance, transfer, statement and PIN APIs")
public class CustomerController {
    private final AccountService accounts;
    private final TransferService transfers;

    public CustomerController(AccountService accounts, TransferService transfers) {
        this.accounts = accounts;
        this.transfers = transfers;
    }

    @GetMapping("/accounts/balances")
    @Operation(summary = "Get balances for current customer")
    public ApiResponse<List<AccountResponse>> balances(Authentication authentication) {
        return ApiResponse.ok("Balances retrieved", accounts.balances(authentication.getName()));
    }

    @PutMapping("/accounts/{id}/pin")
    @Operation(summary = "Change transaction PIN")
    public ApiResponse<Void> pin(
        @PathVariable Long id,
        @Valid @RequestBody ChangePinRequest request,
        Authentication authentication
    ) {
        accounts.changePin(id, authentication.getName(), request);
        return ApiResponse.ok("PIN changed", null);
    }

    @PostMapping("/transactions/transfer")
    @Operation(summary = "Transfer money internally or to an interbank beneficiary")
    public ApiResponse<TransactionResponse> transfer(
        @Valid @RequestBody TransferRequest request,
        Authentication authentication
    ) {
        return ApiResponse.ok("Transfer successful", transfers.transfer(authentication.getName(), request));
    }

    @GetMapping("/accounts/{id}/transactions")
    @Operation(summary = "Get paginated transaction statement")
    public ApiResponse<Page<TransactionResponse>> statement(
        @PathVariable Long id,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        Authentication authentication
    ) {
        return ApiResponse.ok(
            "Statement retrieved",
            transfers.statement(authentication.getName(), id, from, to, pageable)
        );
    }
}
