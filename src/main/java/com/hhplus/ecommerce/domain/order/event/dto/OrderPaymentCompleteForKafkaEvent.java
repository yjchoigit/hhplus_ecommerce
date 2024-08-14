package com.hhplus.ecommerce.domain.order.event.dto;


import com.hhplus.ecommerce.domain.order.entity.Order;
import com.hhplus.ecommerce.domain.payment.entity.Payment;

import java.io.Serializable;

public record OrderPaymentCompleteForKafkaEvent(
        Long buyerId,
        String orderNumber,
        String buyerName,
        String orderCreateDatetime,
        Long paymentId,
        int paymentPrice,
        String paymentCreateDatetime
) implements Serializable {
    public static OrderPaymentCompleteForKafkaEvent toPayload(Long buyerId, Payment payment){
        Order order = payment.getOrder();
        return new OrderPaymentCompleteForKafkaEvent(buyerId, order.getOrderNumber(),
                order.getBuyerName(), order.getCreateDatetime().toString(), payment.getPaymentId(),
                payment.getPaymentPrice(), payment.getCreateDatetime().toString());
    }
}
