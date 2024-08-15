package com.hhplus.ecommerce.service.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hhplus.ecommerce.domain.buyer.entity.Buyer;
import com.hhplus.ecommerce.domain.outbox.OutboxEnums;
import com.hhplus.ecommerce.domain.outbox.entity.Outbox;
import com.hhplus.ecommerce.domain.outbox.repository.OutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OutboxServiceTest {

    @InjectMocks
    private OutboxServiceImpl outboxServiceImpl;

    @Mock
    private OutboxRepository outboxRepository;

    private Buyer buyer;
    private Outbox outbox;

    @BeforeEach
    void setUp() {
        // 회원 등록
        buyer = new Buyer(1L, "홍길동");
        outbox = Outbox.builder()
                .outboxId(1L)
                .relationId(1L)
                .eventType(OutboxEnums.EventType.ORDER_PAYMENT_COMPLETE)
                .payload("test-payload")
                .status(OutboxEnums.Status.INIT)
                .build();
    }

    @Test
    @DisplayName("아웃박스 등록 성공")
    void addOutbox_success(){
        // when
        when(outboxRepository.save(any(Outbox.class))).thenReturn(outbox);

        // then
        Outbox result = outboxServiceImpl.addOutbox(Outbox.builder()
                .relationId(1L)
                .eventType(OutboxEnums.EventType.ORDER_PAYMENT_COMPLETE)
                .payload(serializeEvent(buyer))
                .status(OutboxEnums.Status.INIT)
                .build());

        assertEquals(result.getEventType(), OutboxEnums.EventType.ORDER_PAYMENT_COMPLETE);
    }

    @Test
    @DisplayName("아웃박스 등록 실패")
    void addOutbox_fail(){
        // given
        Outbox addOutbox = Outbox.builder()
                .relationId(1L)
                .payload(serializeEvent(buyer))
                .status(OutboxEnums.Status.INIT)
                .build();
        // when
        when(outboxRepository.save(any(Outbox.class))).thenReturn(addOutbox);

        // then
        Outbox result = outboxServiceImpl.addOutbox(Outbox.builder()
                .relationId(1L)
                .payload(serializeEvent(buyer))
                .status(OutboxEnums.Status.INIT)
                .build());

        assertNotEquals(result.getEventType(), OutboxEnums.EventType.ORDER_PAYMENT_COMPLETE);
    }

    @Test
    @DisplayName("아웃박스 발행 상태 업데이트 성공")
    void updateOutbox_success(){
        // given
        Outbox updatOutbox = Outbox.builder()
                .outboxId(1L)
                .relationId(1L)
                .eventType(OutboxEnums.EventType.ORDER_PAYMENT_COMPLETE)
                .payload("test-payload")
                .status(OutboxEnums.Status.PUBLISHED)
                .build();
        // when
        when(outboxRepository.save(any(Outbox.class))).thenReturn(updatOutbox);

        // then
        Outbox result = outboxServiceImpl.addOutbox(Outbox.builder()
                .outboxId(1L)
                .relationId(1L)
                .eventType(OutboxEnums.EventType.ORDER_PAYMENT_COMPLETE)
                .payload("test-payload")
                .status(OutboxEnums.Status.PUBLISHED)
                .build());

        assertEquals(result.getStatus(), OutboxEnums.Status.PUBLISHED);
    }

    @Test
    @DisplayName("아웃박스 초기 상태 목록 조회 성공")
    void findByInitStatus_success(){
        // given
        // when
        when(outboxRepository.findByInitStatus()).thenReturn(List.of(outbox));

        // then
        List<Outbox> result = outboxServiceImpl.findByInitStatus();

        assertTrue(!result.isEmpty());
    }

    @Test
    @DisplayName("아웃박스 상세 조회 성공")
    void findById_success(){
        // given
        // when
        when(outboxRepository.findById(any())).thenReturn(outbox);

        // then
        Outbox result = outboxServiceImpl.findById(1L);

        assertNotNull(result);
    }

    @Test
    @DisplayName("아웃박스 상세 조회 실패")
    void findById_fail(){
        // given
        // when
        when(outboxRepository.findById(any())).thenReturn(null);

        // then
        Outbox result = outboxServiceImpl.findById(1L);

        assertNull(result);
    }

    private String serializeEvent(Object data) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
}


