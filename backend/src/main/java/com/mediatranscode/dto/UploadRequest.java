package com.mediatranscode.dto;

import lombok.Data;

@Data
public class UploadRequest {
    private String quality;
    private String format;
    private boolean addSubtitles;
    private boolean extractMetadata;
    private boolean optimizeForWeb;
    private boolean optimizeForMobile;
} 