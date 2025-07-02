package com.mediatranscoder.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                if ("dev".equals(activeProfile)) {
                    // Development: Allow all localhost ports
                    registry.addMapping("/**")
                            .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                            .allowedHeaders("*")
                            .exposedHeaders("Content-Disposition", "Content-Type")
                            .allowCredentials(true);
                } else {
                    // Production: Allow Vercel frontend
                    registry.addMapping("/**")
                            .allowedOrigins("https://cloud-transcode.vercel.app")
                            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                            .allowedHeaders("*")
                            .exposedHeaders("Content-Disposition", "Content-Type")
                            .allowCredentials(true);
                }
            }
        };
    }
} 