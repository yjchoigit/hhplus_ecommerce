package com.hhplus.ecommerce.base.config.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class KafkaConsumer {

    @KafkaListener(topics = "sample_topic_1", groupId = "kafka-group-1")
    public void consume(String message) throws IOException {
        log.info("Consumed message : {}", message);
    }
}