package com.mediatranscoder.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;

public interface FileStorageService {
    String storeFile(MultipartFile file) throws IOException;
    String storeFile(File file) throws IOException;
    File downloadFile(String fileKey) throws IOException;
    String uploadFile(File file, String format) throws IOException;
    void deleteFile(String fileKey) throws IOException;
    String getFileUrl(String fileKey);
} 