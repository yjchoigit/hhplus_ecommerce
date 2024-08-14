package com.hhplus.ecommerce.base.config.kafka;

import com.hhplus.ecommerce.domain.outbox.entity.Outbox;
import com.hhplus.ecommerce.service.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventProcessor {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxService outboxService;

    @Transactional
    public void processOutboxEvents(){
        List<Outbox> outboxList = outboxService.findByInitStatus();

        for (Outbox outbox : outboxList) {
            try {
                // 상태 업데이트 (예: PUBLISHED로 변경) 및 로깅
                outbox.markAsPublished();
                outboxService.updateOutbox(outbox);
                log.info("Processed OutboxEvent with ID: {}", outbox.getOutboxId());

                kafkaTemplate.send("order_payment_complete_topic", String.valueOf(outbox.getOutboxId()));

            } catch (Exception e) {
                log.error("Failed to process OutboxEvent with ID: {}", outbox.getOutboxId(), e);
            }
        }
    }
}
