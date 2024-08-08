package com.hhplus.ecommerce.domain.order.entity;

import com.hhplus.ecommerce.domain.base.entity.CreateModifyDateTimeEntity;
import com.hhplus.ecommerce.domain.order.OrderEnums;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class OrderItemSheet extends CreateModifyDateTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("주문서 품목 id")
    private Long orderItemSheetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_sheet_id")
    @Comment("주문서 id")
    private OrderSheet orderSheet;

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
    private int productPrice;

    @Column(nullable = false)
    @Comment("구매 수량")
    private int buyCnt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("주문 상태 Enum")
    private OrderEnums.Status status;

    public OrderItemSheet(Long orderItemSheetId, OrderSheet orderSheet, Long productId, String productName, Long productOptionId, String productOptionName, int productPrice, int buyCnt, OrderEnums.Status status) {
        this.orderItemSheetId = orderItemSheetId;
        this.orderSheet = orderSheet;
        this.productId = productId;
        this.productName = productName;
        this.productOptionId = productOptionId;
        this.productOptionName = productOptionName;
        this.productPrice = productPrice;
        this.buyCnt = buyCnt;
        this.status = status;
    }

    @Builder
    public OrderItemSheet(OrderSheet orderSheet, Long productId, String productName, Long productOptionId, String productOptionName, int productPrice, int buyCnt, OrderEnums.Status status) {
        this.orderSheet = orderSheet;
        this.productId = productId;
        this.productName = productName;
        this.productOptionId = productOptionId;
        this.productOptionName = productOptionName;
        this.productPrice = productPrice;
        this.buyCnt = buyCnt;
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItemSheet orderItem = (OrderItemSheet) o;
        return Objects.equals(getOrderItemSheetId(), orderItem.getOrderItemSheetId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getOrderItemSheetId());
    }
}