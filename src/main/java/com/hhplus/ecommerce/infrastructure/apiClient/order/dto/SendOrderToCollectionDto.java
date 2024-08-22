package com.hhplus.ecommerce.infrastructure.apiClient.order.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record SendOrderToCollectionDto(
        String orderNumber,
        String buyerName,
        String orderCreateDatetime,
        Long paymentId,
        int paymentPrice,
        String paymentCreateDatetime
) implements Serializable {
}
