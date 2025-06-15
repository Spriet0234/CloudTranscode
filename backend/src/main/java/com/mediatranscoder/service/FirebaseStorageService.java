package com.mediatranscoder.service;

import com.google.cloud.storage.Blob;
import com.google.firebase.cloud.StorageClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class FirebaseStorageService implements FileStorageService {

    private final StorageClient storageClient;

    public FirebaseStorageService(StorageClient storageClient) {
        this.storageClient = storageClient;
    }

    @Override
    public String storeFile(MultipartFile file) throws IOException {
        String fileKey = generateFileKey(file.getOriginalFilename());
        Blob blob = storageClient.bucket().create(fileKey, file.getBytes(), file.getContentType());
        return blob.getName();
    }

    @Override
    public String storeFile(File file) throws IOException {
        String fileKey = generateFileKey(file.getName());
        Blob blob = storageClient.bucket().create(fileKey, Files.readAllBytes(file.toPath()), 
            Files.probeContentType(file.toPath()));
        return blob.getName();
    }

    @Override
    public File downloadFile(String fileKey) throws IOException {
        Blob blob = storageClient.bucket().get(fileKey);
        if (blob == null) {
            throw new IOException("File not found: " + fileKey);
        }
        File tempFile = File.createTempFile("download-", fileKey.substring(fileKey.lastIndexOf(".")));
        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            blob.downloadTo(outputStream);
        }
        return tempFile;
    }

    @Override
    public String uploadFile(File file, String format) throws IOException {
        String fileKey = generateFileKey(file.getName());
        Blob blob = storageClient.bucket().create(fileKey, Files.readAllBytes(file.toPath()), "image/" + format);
        return blob.getName();
    }

    @Override
    public void deleteFile(String fileKey) throws IOException {
        storageClient.bucket().get(fileKey).delete();
    }

    @Override
    public String getFileUrl(String fileKey) {
        Blob blob = storageClient.bucket().get(fileKey);
        return blob.signUrl(7, TimeUnit.DAYS).toString();
    }

    private String generateFileKey(String originalFilename) {
        return UUID.randomUUID().toString() + "/" + originalFilename;
    }
} 