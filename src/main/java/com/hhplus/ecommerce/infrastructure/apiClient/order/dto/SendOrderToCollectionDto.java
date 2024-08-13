package com.hhplus.ecommerce.infrastructure.apiClient.order.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
public record SendOrderToCollectionDto(
        String orderNumber,
        String buyerName,
        LocalDateTime orderCreateDatetime,
        Long paymentId,
        int paymentPrice,
        LocalDateTime paymentCreateDatetime
) implements Serializable {
}
