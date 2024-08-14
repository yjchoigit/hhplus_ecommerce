package com.hhplus.ecommerce.domain.outbox.entity;

import com.hhplus.ecommerce.domain.base.entity.CreateModifyDateTimeEntity;
import com.hhplus.ecommerce.domain.outbox.OutboxEnums;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "outbox")
public class Outbox extends CreateModifyDateTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("outbox id")
    private Long outboxId;
    private String aggregateType;
    private Long aggregateId;
    private String eventType;
    private String payload;
    @Enumerated(EnumType.STRING)
    @Comment("상태 Enum")
    private OutboxEnums.Status status;

    @Builder
    public Outbox(String aggregateType, Long aggregateId, String eventType, String payload, OutboxEnums.Status status) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = status;
    }

    public void markAsPublished(){
        this.status = OutboxEnums.Status.PUBLISHED;
    }

    public void markAsProcessed(){
        this.status = OutboxEnums.Status.PROCESSED;
    }

}
