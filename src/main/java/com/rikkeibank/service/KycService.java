package com.rikkeibank.service;

import com.rikkeibank.domain.KycProfile;
import com.rikkeibank.domain.KycStatus;
import com.rikkeibank.domain.User;
import com.rikkeibank.dto.KycDtos.KycRequest;
import com.rikkeibank.dto.KycDtos.KycResponse;
import com.rikkeibank.dto.KycDtos.ReviewRequest;
import com.rikkeibank.exception.BusinessException;
import com.rikkeibank.repository.KycProfileRepository;
import com.rikkeibank.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
public class KycService {
    private final KycProfileRepository profiles;private final UserRepository users;private final StorageService storage;
    public KycService(KycProfileRepository profiles,UserRepository users,StorageService storage){this.profiles=profiles;this.users=users;this.storage=storage;}
    @Transactional public KycResponse submit(String username,KycRequest req,MultipartFile file){
        User u=users.findByUsername(username).orElseThrow();if(profiles.findByUserUsername(username).isPresent()||profiles.existsByIdNumber(req.idNumber()))throw new BusinessException(HttpStatus.CONFLICT,"KYC profile or ID number already exists");
        KycProfile k=new KycProfile();k.setUser(u);k.setIdNumber(req.idNumber());k.setFullName(req.fullName());k.setDob(req.dob());k.setSex(req.sex());k.setAddress(req.address());k.setIdCardFrontUrl(storage.store(file));return map(profiles.save(k));
    }
    public Page<KycResponse> pending(Pageable p){return profiles.findByStatus(KycStatus.PENDING,p).map(this::map);}
    @Transactional public KycResponse review(Long id,String staff,ReviewRequest req){
        if(req.status()==KycStatus.PENDING)throw new BusinessException(HttpStatus.BAD_REQUEST,"Review status must be CONFIRM or REJECT");
        KycProfile k=profiles.findById(id).orElseThrow(()->new BusinessException(HttpStatus.NOT_FOUND,"KYC profile not found"));
        if(k.getStatus()!=KycStatus.PENDING)throw new BusinessException(HttpStatus.CONFLICT,"KYC profile has already been reviewed");
        k.setStatus(req.status());k.setRejectionReason(req.status()==KycStatus.REJECT?req.reason():null);k.setVerifiedBy(users.findByUsername(staff).orElseThrow());k.setVerifiedAt(LocalDateTime.now());k.getUser().setKyc(req.status()==KycStatus.CONFIRM);return map(k);
    }
    private KycResponse map(KycProfile k){return new KycResponse(k.getId(),k.getUser().getUsername(),k.getIdNumber(),k.getFullName(),k.getIdCardFrontUrl(),k.getStatus(),k.getRejectionReason());}
}
