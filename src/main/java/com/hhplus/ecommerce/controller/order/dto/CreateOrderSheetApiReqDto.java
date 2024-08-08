package com.hhplus.ecommerce.controller.order.dto;

import com.hhplus.ecommerce.service.order.dto.CreateOrderSheetReqDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record CreateOrderSheetApiReqDto(
    @Schema(description = "회원 ID")
    Long buyerId,
    @Schema(description = "주문자명")
    String buyerName,
    @Schema(description = "총 구매수량")
    int allBuyCnt,
    @Schema(description = "총 상품 가격")
    int totalPrice,
    @Schema(description = "장바구니 ID 리스트")
    List<Long> cartIdList,
    @Schema(description = "주문 품목 리스트")
    List<CreateOrderItemSheetApiReqDto> orderItemList
) implements Serializable {

    @Builder
    public record CreateOrderItemSheetApiReqDto(
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

        public CreateOrderSheetReqDto.CreateOrderItemSheetReqDto request(){
            return CreateOrderSheetReqDto.CreateOrderItemSheetReqDto.builder()
                    .productId(productId)
                    .productName(productName)
                    .productOptionId(productOptionId)
                    .productOptionName(productOptionName)
                    .productPrice(productPrice)
                    .buyCnt(buyCnt)
                    .build();
        }

    }

    public CreateOrderSheetReqDto request() {
        return CreateOrderSheetReqDto.builder()
                .buyerId(buyerId)
                .buyerName(buyerName)
                .allBuyCnt(allBuyCnt)
                .totalPrice(totalPrice)
                .cartIdList(cartIdList)
                .orderItemList(orderItemList.stream().map(CreateOrderItemSheetApiReqDto::request).toList())
                .build();
    }
}