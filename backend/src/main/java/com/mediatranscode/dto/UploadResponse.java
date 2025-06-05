package com.mediatranscode.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadResponse {
    private String id;
    private String filename;
    private String status;
    private Integer progress;
    private Long fileSize;
    private String message;
} 