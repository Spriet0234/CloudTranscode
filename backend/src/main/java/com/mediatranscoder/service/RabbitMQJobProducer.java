package com.mediatranscoder.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediatranscoder.model.Job;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RabbitMQJobProducer {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.rabbitmq.queue:media_jobs}")
    private String queueName;

    public void sendJob(Job job, String inputUrl) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("id", job.getId().toString());
            message.put("input_url", inputUrl);
            message.put("output_format", job.getOutputFormat());
            message.put("output_quality", job.getOutputQuality());
            message.put("settings", job.getSettings());
            message.put("callback_url", "http://backend:8080/api/v1/jobs/worker-callback"); // adjust as needed
            String json = objectMapper.writeValueAsString(message);
            rabbitTemplate.convertAndSend(queueName, json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send job to RabbitMQ", e);
        }
    }
} 