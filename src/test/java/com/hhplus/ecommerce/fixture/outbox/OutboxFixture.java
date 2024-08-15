package com.hhplus.ecommerce.fixture.outbox;

import com.hhplus.ecommerce.domain.outbox.OutboxEnums;
import com.hhplus.ecommerce.domain.outbox.entity.Outbox;
import com.hhplus.ecommerce.domain.outbox.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OutboxFixture {
    @Autowired
    private OutboxRepository outboxRepository;

    @Transactional
    public Outbox add_outbox(LocalDateTime createDatetime){
        Outbox outbox = outboxRepository.save(Outbox.builder()
                .relationId(1L)
                .eventType(OutboxEnums.EventType.ORDER_PAYMENT_COMPLETE)
                .payload("test-payload")
                .status(OutboxEnums.Status.INIT)
                .build());

        ReflectionTestUtils.setField(outbox, "createDatetime", createDatetime);
        return outboxRepository.save(outbox);
    }
}
