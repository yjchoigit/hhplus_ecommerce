package com.hhplus.ecommerce.domain.payment.event.dto;

import com.hhplus.ecommerce.domain.payment.entity.Payment;

public record PayEvent(
        Long buyerId,
        Long orderId
) {
}
