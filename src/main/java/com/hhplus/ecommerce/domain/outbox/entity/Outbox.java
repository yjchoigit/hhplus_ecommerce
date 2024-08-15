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
@Builder
@Table(name = "outbox")
public class Outbox extends CreateModifyDateTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("아웃박스 Id")
    private Long outboxId;

    @Comment("연관 Id")
    private Long relationId;

    @Enumerated(EnumType.STRING)
    @Comment("이벤트 타입 Enum")
    @Column(nullable = false)
    private OutboxEnums.EventType eventType;

    @Comment("페이로드")
    @Column(nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("상태 Enum")
    private OutboxEnums.Status status;

    public Outbox(Long relationId, OutboxEnums.EventType eventType, String payload, OutboxEnums.Status status) {
        this.relationId = relationId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = status;
    }

    public Outbox(Long outboxId, Long relationId, OutboxEnums.EventType eventType, String payload, OutboxEnums.Status status) {
        this.outboxId = outboxId;
        this.relationId = relationId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = status;
    }

    public void markAsPublished(){
        this.status = OutboxEnums.Status.PUBLISHED;
    }

}
