package com.hhplus.ecommerce.domain.outbox.repository;

import com.hhplus.ecommerce.domain.outbox.OutboxEnums;
import com.hhplus.ecommerce.domain.outbox.entity.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxJpaRepository extends JpaRepository<Outbox, Long> {
    List<Outbox> findByStatus(OutboxEnums.Status status);
}
