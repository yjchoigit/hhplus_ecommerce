package com.hhplus.ecommerce.scheduler.outbox;

import com.hhplus.ecommerce.domain.outbox.entity.Outbox;
import com.hhplus.ecommerce.event.kafka.KafkaConstants;
import com.hhplus.ecommerce.service.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventProcessor {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OutboxService outboxService;

    @Transactional
    public void processOutboxEvents(){
        LocalDateTime now = LocalDateTime.now();

        List<Outbox> outboxList = outboxService.findByInitStatus();

        for (Outbox outbox : outboxList) {
            if(outbox.getCreateDatetime().isBefore(now.minusMinutes(10))
                || (outbox.getModifyDatetime() != null && outbox.getModifyDatetime().isBefore(now.minusMinutes(10)))) {

                log.info("Retry Publish OutboxEvent:{}, with ID: {}", outbox.getEventType(), outbox.getOutboxId());
                kafkaTemplate.send(KafkaConstants.ORDER_PAYMENT_COMPLETE_TOPIC, String.valueOf(outbox.getOutboxId()));
            }
        }
    }
}
