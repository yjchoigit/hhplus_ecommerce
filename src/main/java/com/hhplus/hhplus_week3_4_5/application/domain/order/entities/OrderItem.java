package com.hhplus.hhplus_week3_4_5.application.domain.order.entities;

import com.hhplus.hhplus_week3_4_5.application.domain.base.entities.CreateModifyDateTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class OrderItem extends CreateModifyDateTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("주문 품목 id")
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @Comment("주문 id")
    private Orders orders;

    @Column(nullable = false)
    @Comment("상품 id")
    private Long productId;

    @Column(nullable = false)
    @Comment("상품명")
    private String productName;

    @Comment("상품 옵션 id")
    private Long productOptionId;

    @Comment("상품 옵션명")
    private String productOptionName;

    @Column(nullable = false)
    @Comment("상품 가격")
    private BigDecimal productPrice;

    @Column(nullable = false)
    @Comment("구매 수량")
    private int buyCnt;

    @Builder
    public OrderItem(Long orderItemId, Orders orders, Long productId, String productName, Long productOptionId, String productOptionName, BigDecimal productPrice, int buyCnt) {
        this.orderItemId = orderItemId;
        this.orders = orders;
        this.productId = productId;
        this.productName = productName;
        this.productOptionId = productOptionId;
        this.productOptionName = productOptionName;
        this.productPrice = productPrice;
        this.buyCnt = buyCnt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Objects.equals(getOrderItemId(), orderItem.getOrderItemId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getOrderItemId());
    }
}