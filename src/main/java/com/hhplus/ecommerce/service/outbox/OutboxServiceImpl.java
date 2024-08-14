package com.hhplus.ecommerce.service.outbox;

import com.hhplus.ecommerce.domain.outbox.entity.Outbox;
import com.hhplus.ecommerce.domain.outbox.repository.OutboxRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional(rollbackFor = {Exception.class}, readOnly = true)
public class OutboxServiceImpl implements OutboxService {
    private OutboxRepository outboxRepository;

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Outbox addOutbox(Outbox outbox){
        return outboxRepository.save(outbox);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Outbox updateOutbox(Outbox outbox) {
        return outboxRepository.save(outbox);
    }

    @Override
    public List<Outbox> findByInitStatus() {
        return outboxRepository.findByInitStatus();
    }

    @Override
    public Outbox findById(Long outboxId) {
        return outboxRepository.findById(outboxId);
    }

}
