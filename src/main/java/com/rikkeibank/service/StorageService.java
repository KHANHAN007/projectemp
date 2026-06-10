package com.rikkeibank.service;

import org.springframework.web.multipart.MultipartFile;
public interface StorageService { String store(MultipartFile file); }
