package com.rikkeibank.service;

import com.rikkeibank.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {
    private final Path root;
    public LocalStorageService(@Value("${app.storage.upload-dir}")String dir){root=Path.of(dir).toAbsolutePath().normalize();}
    public String store(MultipartFile file){
        if(file.isEmpty()||file.getContentType()==null||!Set.of("image/jpeg","image/png","application/pdf").contains(file.getContentType()))
            throw new BusinessException(HttpStatus.BAD_REQUEST,"eKYC file must be JPEG, PNG or PDF");
        try{Files.createDirectories(root);String name=UUID.randomUUID()+"-"+Path.of(file.getOriginalFilename()==null?"kyc":file.getOriginalFilename()).getFileName();Files.copy(file.getInputStream(),root.resolve(name));return root.resolve(name).toUri().toString();}
        catch(Exception e){throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE,"Cannot store eKYC document");}
    }
}
