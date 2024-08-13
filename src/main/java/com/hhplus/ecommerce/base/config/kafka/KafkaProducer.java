package com.hhplus.ecommerce.base.config.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducer {
    private static final String TOPIC = "sample_topic_1";
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String message) {
        log.info("Produce message : {}", message);
        this.kafkaTemplate.send(TOPIC, message);
    }

    public void sendMessageCallback(String message) {
        CompletableFuture<SendResult<String, String>> future = this.kafkaTemplate.send(TOPIC, message);
        future.whenComplete((result, ex) -> {
           if (ex == null) {
               log.info("Send message callback success");
           } else {
                log.error("Send message callback error", ex);
           }
        });
    }
}