package com.rikkeibank.repository;

import com.rikkeibank.model.BankTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<BankTransaction, Long> {
    @Query("""
        select t from BankTransaction t
        where (t.fromAccount.id=:accountId or t.toAccount.id=:accountId)
          and (:fromDate is null or t.createdAt >= :fromDate)
          and (:toDate is null or t.createdAt <= :toDate)
        """)
    Page<BankTransaction> statement(@Param("accountId") Long accountId,
        @Param("fromDate") LocalDateTime fromDate,
        @Param("toDate") LocalDateTime toDate,
        Pageable pageable);
}
