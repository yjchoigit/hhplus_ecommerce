package com.hhplus.ecommerce.domain.point.event.dto;

import com.hhplus.ecommerce.domain.payment.entity.Payment;

public record UsePointEvent(
        Long buyerId,
        Payment payment
) {
}
