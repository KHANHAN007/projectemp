package com.rikkeibank.service;

import com.rikkeibank.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class LocalStorageService implements StorageService {
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "image/jpeg",
        "image/png",
        "application/pdf"
    );

    private final Path root;

    public LocalStorageService(@Value("${app.storage.upload-dir}") String dir) {
        this.root = Path.of(dir).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file) {
        if (file.isEmpty() || file.getContentType() == null || !ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "eKYC file must be JPEG, PNG or PDF");
        }

        try {
            Files.createDirectories(root);
            String originalName = file.getOriginalFilename() == null ? "kyc" : file.getOriginalFilename();
            String storedName = UUID.randomUUID() + "-" + Path.of(originalName).getFileName();
            Path destination = root.resolve(storedName);
            Files.copy(file.getInputStream(), destination);
            log.info("Stored eKYC file path={}", destination);
            return destination.toUri().toString();
        } catch (Exception e) {
            log.error("Cannot store eKYC document", e);
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "Cannot store eKYC document");
        }
    }
}
