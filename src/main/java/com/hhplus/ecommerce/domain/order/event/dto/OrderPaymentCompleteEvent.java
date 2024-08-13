package com.hhplus.ecommerce.domain.order.event.dto;

import com.hhplus.ecommerce.domain.payment.entity.Payment;

public record OrderPaymentCompleteEvent(
        Long buyerId,
        Payment payment
) {
}
