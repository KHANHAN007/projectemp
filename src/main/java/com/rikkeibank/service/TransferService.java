package com.rikkeibank.service;

import com.rikkeibank.domain.Account;
import com.rikkeibank.domain.BankTransaction;
import com.rikkeibank.domain.TransactionStatus;
import com.rikkeibank.domain.TransactionType;
import com.rikkeibank.dto.TransactionDtos.TransactionResponse;
import com.rikkeibank.dto.TransactionDtos.TransferRequest;
import com.rikkeibank.exception.BusinessException;
import com.rikkeibank.repository.AccountRepository;
import com.rikkeibank.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransferService {
    private final AccountRepository accounts;private final TransactionRepository transactions;private final PasswordEncoder encoder;
    public TransferService(AccountRepository accounts,TransactionRepository transactions,PasswordEncoder encoder){this.accounts=accounts;this.transactions=transactions;this.encoder=encoder;}
    @Transactional public TransactionResponse transfer(String username,TransferRequest req){
        Account owned=accounts.findByIdAndUserUsername(req.fromAccountId(),username).orElseThrow(()->new BusinessException(HttpStatus.FORBIDDEN,"Source account is not owned by current user"));
        if(!owned.isActive())throw new BusinessException(HttpStatus.CONFLICT,"Source account is inactive");
        if(!owned.getUser().isKyc())throw new BusinessException(HttpStatus.FORBIDDEN,"KYC approval is required");
        if(!encoder.matches(req.pin(),owned.getTransactionPin()))throw new BusinessException(HttpStatus.UNAUTHORIZED,"Invalid transaction PIN");
        Account from=accounts.findByIdForUpdate(req.fromAccountId()).orElseThrow();
        if(!from.isActive())throw new BusinessException(HttpStatus.CONFLICT,"Source account is inactive");
        if(from.getBalance().compareTo(req.amount())<0)throw new BusinessException(HttpStatus.CONFLICT,"Insufficient balance");
        Account to=null; TransactionType type;
        if(req.toAccountId()!=null){if(req.toAccountId().equals(from.getId()))throw new BusinessException(HttpStatus.BAD_REQUEST,"Source and destination must differ");
            to=accounts.findByIdForUpdate(req.toAccountId()).orElseThrow(()->new BusinessException(HttpStatus.NOT_FOUND,"Destination account not found"));
            if(!to.isActive())throw new BusinessException(HttpStatus.CONFLICT,"Destination account is inactive");type=TransactionType.INTERNAL;
        }else{if(isBlank(req.beneficiaryBank())||isBlank(req.beneficiaryAccount()))throw new BusinessException(HttpStatus.BAD_REQUEST,"Interbank beneficiary is required");type=TransactionType.INTERBANK;}
        from.setBalance(from.getBalance().subtract(req.amount()));if(to!=null)to.setBalance(to.getBalance().add(req.amount()));
        BankTransaction tx=new BankTransaction();tx.setTransactionCode("TX-"+UUID.randomUUID());tx.setFromAccount(from);tx.setToAccount(to);
        tx.setBeneficiaryBank(req.beneficiaryBank());tx.setBeneficiaryAccount(req.beneficiaryAccount());tx.setAmount(req.amount());
        tx.setDescription(req.description());tx.setType(type);tx.setStatus(TransactionStatus.SUCCESS);tx.setCreatedAt(LocalDateTime.now());
        return map(transactions.save(tx),from.getId());
    }
    public Page<TransactionResponse> statement(String username,Long accountId,LocalDateTime fromDate,LocalDateTime toDate,Pageable pageable){
        if(fromDate!=null&&toDate!=null&&fromDate.isAfter(toDate))throw new BusinessException(HttpStatus.BAD_REQUEST,"from must be before to");
        accounts.findByIdAndUserUsername(accountId,username).orElseThrow(()->new BusinessException(HttpStatus.FORBIDDEN,"Account is not owned by current user"));
        return transactions.statement(accountId,fromDate,toDate,pageable).map(t->map(t,accountId));
    }
    private TransactionResponse map(BankTransaction t,Long accountId){return new TransactionResponse(t.getTransactionCode(),t.getFromAccount().getId(),
        t.getToAccount()==null?null:t.getToAccount().getId(),t.getAmount(),t.getDescription(),t.getType().name(),t.getStatus().name(),
        t.getFromAccount().getId().equals(accountId)?"DEBIT":"CREDIT",t.getCreatedAt());}
    private boolean isBlank(String value){return value==null||value.trim().isEmpty();}
}
