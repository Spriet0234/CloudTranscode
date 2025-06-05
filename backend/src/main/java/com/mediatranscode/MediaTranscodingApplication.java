package com.mediatranscode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MediaTranscodingApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediaTranscodingApplication.class, args);
    }
} 