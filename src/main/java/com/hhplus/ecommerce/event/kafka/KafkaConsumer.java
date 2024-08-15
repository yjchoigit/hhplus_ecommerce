package com.hhplus.ecommerce.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hhplus.ecommerce.domain.order.event.OrderEventPublish;
import com.hhplus.ecommerce.domain.order.event.dto.OrderPaymentCompleteForKafkaEvent;
import com.hhplus.ecommerce.domain.outbox.OutboxEnums;
import com.hhplus.ecommerce.domain.outbox.entity.Outbox;
import com.hhplus.ecommerce.service.outbox.OutboxService;
import com.hhplus.ecommerce.service.payment.PaymentService;
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

    @KafkaListener(topics = KafkaConstants.ORDER_PAYMENT_COMPLETE_TOPIC, groupId = KafkaConstants.ORDER_GROUP)
    public void orderPaymentCompleteConsume(String message) {
        Long outboxId = Long.valueOf(message);
        // 조회
        Outbox outbox = outboxService.findById(outboxId);
        // Outbox 상태 업데이트
        outbox.markAsPublished();
        outboxService.updateOutbox(outbox);

        // 수신된 메시지를 처리하고, 외부 API로 데이터를 전송
        try {
            // 주문 데이터 외부 플랫폼 전달
            OrderPaymentCompleteForKafkaEvent event = parseMessage(outbox.getPayload());
            // 외부 API로 데이터 전송 로직
            orderEventPublish.orderPaymentCompleteForKafka(event);

            log.info("Processed OutboxEvent : {}, with ID: {}", outbox.getEventType(), outboxId);
        } catch (Exception e) {
            log.error("Failed to process message: {}", message, e);
        }
    }

    private OrderPaymentCompleteForKafkaEvent parseMessage(String message) {
        try {
            return new ObjectMapper().readValue(message, OrderPaymentCompleteForKafkaEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize event payload", e);
        }
    }
}