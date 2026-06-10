package com.rikkeibank.repository;

import com.rikkeibank.domain.KycProfile;
import com.rikkeibank.domain.KycStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KycProfileRepository extends JpaRepository<KycProfile, Long> {
    Optional<KycProfile> findByUserUsername(String username);
    Page<KycProfile> findByStatus(KycStatus status, Pageable pageable);
    boolean existsByIdNumber(String idNumber);
}
