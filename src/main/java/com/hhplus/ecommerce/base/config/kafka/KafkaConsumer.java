package com.hhplus.ecommerce.base.config.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hhplus.ecommerce.domain.order.event.OrderEventPublish;
import com.hhplus.ecommerce.domain.order.event.dto.OrderPaymentCompleteForKafkaEvent;
import com.hhplus.ecommerce.domain.outbox.OutboxEnums;
import com.hhplus.ecommerce.domain.outbox.entity.Outbox;
import com.hhplus.ecommerce.service.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

    private final OutboxService outboxService;
    private final OrderEventPublish orderEventPublish;

    @KafkaListener(topics = "order_payment_complete_topic", groupId = "order-group")
    public void orderConsume(String message) {
        // 수신된 메시지를 처리하고, 외부 API로 데이터를 전송
        try {
            Long outboxId = Long.valueOf(message);

            // 조회
            Outbox outbox = outboxService.findById(outboxId);
            // PUBLISHED 상태가 아니면 처리하지 않음
            if(!OutboxEnums.Status.PUBLISHED.equals(outbox.getStatus())){
                log.warn("Outbox event with ID {} is not in PUBLISHED state. Current state: {}", outboxId, outbox.getStatus());
                return;
            }
            sendOrderDataToExternalPlatform(outbox);

            // Outbox 상태 업데이트 (이벤트 처리 완료)
            outbox.markAsProcessed();
            outboxService.updateOutbox(outbox);

            log.info("Processed OutboxEvent with ID: {}", outboxId);
        } catch (Exception e) {
            log.error("Failed to process message: {}", message, e);
        }
    }

    private void sendOrderDataToExternalPlatform(Outbox outbox){
        OrderPaymentCompleteForKafkaEvent event = parseMessage(outbox.getPayload());

        // 외부 API로 데이터 전송 로직 (기존 sendOrderPaymentInfo 로직)
        orderEventPublish.orderPaymentCompleteForKafka(event);
    }

    private OrderPaymentCompleteForKafkaEvent parseMessage(String message) {
        try {
            return new ObjectMapper().readValue(message, OrderPaymentCompleteForKafkaEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize event payload", e);
        }
    }
}