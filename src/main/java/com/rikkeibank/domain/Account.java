package com.rikkeibank.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
@Getter @Setter
public class Account extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;
    @Column(nullable = false, unique = true)
    private String accountNumber;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;
    @Column(nullable = false, length = 3)
    private String currency = "VND";
    @Column(nullable = false)
    private String transactionPin;
    @Column(nullable = false)
    private boolean active = true;
    @Version
    private long version;
}
