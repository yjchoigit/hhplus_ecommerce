package com.hhplus.ecommerce.base.config.kafka;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class OutboxEventScheduler {
    private final OutboxEventProcessor outboxEventProcessor;

    public OutboxEventScheduler(OutboxEventProcessor outboxEventProcessor) {
        this.outboxEventProcessor = outboxEventProcessor;
    }

    @Scheduled(fixedRate = 10000)
    public void processOutboxEvents() {
        outboxEventProcessor.processOutboxEvents();
    }
}
