package com.rikkeibank.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter @Setter
public class BankTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String transactionCode;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_account_id")
    private Account fromAccount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    private Account toAccount;
    private String beneficiaryBank;
    private String beneficiaryAccount;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    private String description;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private TransactionType type;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private TransactionStatus status;
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist void create() { if (createdAt == null) createdAt = LocalDateTime.now(); }
}
