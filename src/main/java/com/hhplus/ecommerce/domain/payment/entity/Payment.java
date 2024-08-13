package com.hhplus.ecommerce.domain.payment.entity;

import com.hhplus.ecommerce.domain.base.entity.CreateModifyDateTimeEntity;
import com.hhplus.ecommerce.domain.order.OrderEnums;
import com.hhplus.ecommerce.domain.order.entity.Order;
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
public class Payment extends CreateModifyDateTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("주문 결제 id")
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @Comment("주문 id")
    private Order order;

    @Column(nullable = false)
    @Comment("결제 금액")
    private int paymentPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("결제 상태 Enum")
    private OrderEnums.PaymentStatus status;

    public Payment(Long paymentId, Order order, int paymentPrice, OrderEnums.PaymentStatus status) {
        this.paymentId = paymentId;
        this.order = order;
        this.paymentPrice = paymentPrice;
        this.status = status;
    }

    @Builder
    public Payment(Order order, int paymentPrice, OrderEnums.PaymentStatus status) {
        this.order = order;
        this.paymentPrice = paymentPrice;
        this.status = status;
    }

    // 결제 대기 상태로 변경
    public void paymentWait(){
        this.status = OrderEnums.PaymentStatus.WAIT;
    }

    // 결제 완료 상태로 변경
    public void paymentComplete(){
        this.status = OrderEnums.PaymentStatus.PAY_COMPLETE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment that = (Payment) o;
        return Objects.equals(getPaymentId(), that.getPaymentId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getPaymentId());
    }
}
