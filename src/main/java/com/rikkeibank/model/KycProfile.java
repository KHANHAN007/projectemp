package com.rikkeibank.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "kyc_profiles")
@Getter @Setter
public class KycProfile extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private User user;
    @Column(nullable = false, unique = true)
    private String idNumber;
    @Column(nullable = false)
    private String fullName;
    @Column(nullable = false)
    private LocalDate dob;
    @Column(nullable = false)
    private String sex;
    @Column(nullable = false)
    private String address;
    @Column(nullable = false, length = 500)
    private String idCardFrontUrl;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private KycStatus status = KycStatus.PENDING;
    private String rejectionReason;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private User verifiedBy;
    private LocalDateTime verifiedAt;
}
