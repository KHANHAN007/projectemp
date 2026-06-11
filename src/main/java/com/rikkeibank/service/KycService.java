package com.rikkeibank.service;

import com.rikkeibank.model.KycProfile;
import com.rikkeibank.model.KycStatus;
import com.rikkeibank.model.User;
import com.rikkeibank.dto.KycDtos.KycRequest;
import com.rikkeibank.dto.KycDtos.KycResponse;
import com.rikkeibank.dto.KycDtos.ReviewRequest;
import com.rikkeibank.exception.BusinessException;
import com.rikkeibank.repository.KycProfileRepository;
import com.rikkeibank.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@Slf4j
public class KycService {
    private final KycProfileRepository profiles;
    private final UserRepository users;
    private final StorageService storage;

    public KycService(KycProfileRepository profiles, UserRepository users, StorageService storage) {
        this.profiles = profiles;
        this.users = users;
        this.storage = storage;
    }

    @Transactional
    public KycResponse submit(String username, KycRequest request, MultipartFile file) {
        User user = users.findByUsername(username).orElseThrow();
        boolean profileExists = profiles.findByUserUsername(username).isPresent();
        boolean idNumberExists = profiles.existsByIdNumber(request.idNumber());

        if (profileExists || idNumberExists) {
            log.warn("Rejected duplicate eKYC username={} idNumber={}", username, request.idNumber());
            throw new BusinessException(HttpStatus.CONFLICT, "KYC profile or ID number already exists");
        }

        KycProfile profile = new KycProfile();
        profile.setUser(user);
        profile.setIdNumber(request.idNumber());
        profile.setFullName(request.fullName());
        profile.setDob(request.dob());
        profile.setSex(request.sex());
        profile.setAddress(request.address());
        profile.setIdCardFrontUrl(storage.store(file));

        KycProfile saved = profiles.save(profile);
        log.info("Submitted eKYC id={} username={}", saved.getId(), username);
        return map(saved);
    }

    @Transactional(readOnly = true)
    public Page<KycResponse> pending(Pageable pageable) {
        log.debug("Listing pending eKYC page request={}", pageable);
        return profiles.findByStatus(KycStatus.PENDING, pageable).map(this::map);
    }

    @Transactional
    public KycResponse review(Long id, String staff, ReviewRequest request) {
        if (request.status() == KycStatus.PENDING) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Review status must be CONFIRM or REJECT");
        }

        KycProfile profile = profiles.findById(id)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "KYC profile not found"));

        if (profile.getStatus() != KycStatus.PENDING) {
            throw new BusinessException(HttpStatus.CONFLICT, "KYC profile has already been reviewed");
        }

        profile.setStatus(request.status());
        profile.setRejectionReason(request.status() == KycStatus.REJECT ? request.reason() : null);
        profile.setVerifiedBy(users.findByUsername(staff).orElseThrow());
        profile.setVerifiedAt(LocalDateTime.now());
        profile.getUser().setKyc(request.status() == KycStatus.CONFIRM);

        log.info("Reviewed eKYC id={} status={} staff={}", id, request.status(), staff);
        return map(profile);
    }

    private KycResponse map(KycProfile profile) {
        return new KycResponse(
            profile.getId(),
            profile.getUser().getUsername(),
            profile.getIdNumber(),
            profile.getFullName(),
            profile.getIdCardFrontUrl(),
            profile.getStatus(),
            profile.getRejectionReason()
        );
    }
}
