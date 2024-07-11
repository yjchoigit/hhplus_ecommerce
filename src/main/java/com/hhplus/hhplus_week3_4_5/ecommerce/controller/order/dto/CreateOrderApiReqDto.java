package com.hhplus.hhplus_week3_4_5.ecommerce.controller.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

public record CreateOrderApiReqDto(
    @Schema(description = "회원 ID")
    Long buyerId,
    @Schema(description = "주문자명")
    String buyerName,
    @Schema(description = "총 구매수량")
    int allBuyCnt,
    @Schema(description = "총 상품 금액")
    int totalPrice,
    @Schema(description = "주문 품목 리스트")
    List<CreateOrderItemApiReqDto> orderItemList
) implements Serializable {
    public record CreateOrderItemApiReqDto(
            @Schema(description = "상품 ID")
            Long productId,
            @Schema(description = "상품명")
            String productName,
            @Schema(description = "상품 옵션 ID")
            Long productOptionId,
            @Schema(description = "상품 옵션명")
            String productOptionName,
            @Schema(description = "상품 가격")
            int productPrice,
            @Schema(description = "상품 구매수량")
            int buyCnt
    ) implements Serializable {

    }
}
