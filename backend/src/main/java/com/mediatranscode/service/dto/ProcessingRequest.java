package com.mediatranscode.service.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingRequest {
    private String quality;
    private String format;
    private Boolean addSubtitles;
    private Boolean extractMetadata;
    private Boolean optimizeForWeb;
    private Boolean optimizeForMobile;
    private Integer priority;
} 