package com.hhplus.ecommerce.domain.outbox.repository;

import com.hhplus.ecommerce.domain.outbox.entity.Outbox;

import java.util.List;

public interface OutboxRepository {
    Outbox save(Outbox outbox);

    List<Outbox> findByInitStatus();

    Outbox findById(Long outboxId);
}
