package com.hhplus.hhplus_week3_4_5.ecommerce.controller.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record GetOrderApiResDto(
    @Schema(description = "주문 ID")
    Long orderId,
    @Schema(description = "주문번호")
    String orderNumber,
    @Schema(description = "총 구매 수량")
    int allBuyCnt,
    @Schema(description = "주문 일시")
    LocalDateTime createDatetime,
    @Schema(description = "주문 품목 리스트")
    List<GetOrderItemApiResDto> items

) implements Serializable {
    
    public record GetOrderItemApiResDto(
            @Schema(description = "주문 품목 ID")
            Long orderItemId,
            @Schema(description = "상품 ID")
            Long productId,
            @Schema(description = "상품 명")
            Long productName,
            @Schema(description = "상품 옵션 ID")
            Long productOptionId,
            @Schema(description = "상품 옵션 명")
            Long productOptionName,
            @Schema(description = "상품 금액")
            BigDecimal productPrice,
            @Schema(description = "구매 수량")
            int buyCnt
            ) implements Serializable {
    }
}