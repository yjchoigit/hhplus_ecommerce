package com.hhplus.ecommerce.domain.outbox.repository;

import com.hhplus.ecommerce.domain.outbox.OutboxEnums;
import com.hhplus.ecommerce.domain.outbox.entity.Outbox;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OutboxRepositoryImpl implements OutboxRepository {
    private final OutboxJpaRepository outboxJpaRepository;

    public OutboxRepositoryImpl(OutboxJpaRepository outboxJpaRepository) {
        this.outboxJpaRepository = outboxJpaRepository;
    }

    @Override
    public Outbox save(Outbox outbox) {
        return outboxJpaRepository.save(outbox);
    }

    @Override
    public List<Outbox> findByInitStatus() {
        return outboxJpaRepository.findByStatus(OutboxEnums.Status.INIT);
    }

    @Override
    public Outbox findById(Long outboxId) {
        return outboxJpaRepository.findById(outboxId).orElse(null);
    }
}
