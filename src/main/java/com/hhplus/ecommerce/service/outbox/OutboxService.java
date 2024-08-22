package com.hhplus.ecommerce.service.outbox;


import com.hhplus.ecommerce.domain.outbox.entity.Outbox;

import java.util.List;

public interface OutboxService {
    Outbox addOutbox(Outbox outbox);
    Outbox updateOutbox(Outbox outbox);
    List<Outbox> findByInitStatus();
    Outbox findById(Long outboxId);
}
