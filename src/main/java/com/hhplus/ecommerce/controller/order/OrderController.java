package com.hhplus.ecommerce.controller.order;

import com.hhplus.ecommerce.base.exception.reponse.dto.ResponseDto;
import com.hhplus.ecommerce.base.exception.reponse.util.ResponseUtil;
import com.hhplus.ecommerce.controller.order.dto.*;
import com.hhplus.ecommerce.facade.order.OrderPaymentFacade;
import com.hhplus.ecommerce.service.order.OrderService;
import com.hhplus.ecommerce.service.order.OrderSheetService;
import com.hhplus.ecommerce.service.order.dto.CreateOrderSheetResDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "/orders", description = "주문 API")
@RestController
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderPaymentFacade orderPaymentFacade;
    private final OrderSheetService orderSheetService;
    private final OrderService orderService;

    @Operation(summary = "주문서 생성")
    @ApiResponse(responseCode = "200", description = "성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateOrderSheetApiResDto.class)))
    @PostMapping(value = "/orders/sheet")
    public ResponseDto<CreateOrderSheetApiResDto> createOrderSheet(@RequestBody @Valid CreateOrderSheetApiReqDto reqDto){
        CreateOrderSheetResDto resDto = orderSheetService.createOrderSheet(reqDto.request());
        return ResponseUtil.success(CreateOrderSheetApiResDto.from(resDto));
    }

    @Operation(summary = "주문 진행")
    @ApiResponse(responseCode = "200", description = "성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class)))
    @PostMapping(value = "/orders")
    public ResponseDto<Long> createOrder(@RequestBody @Valid CreateOrderApiReqDto reqDto){
        Long orderId = orderPaymentFacade.createOrder(reqDto);
        if(orderId == null){
            return ResponseUtil.failure();
        }
        return ResponseUtil.success(orderId);
    }

    @Operation(summary = "주문 결제 진행")
    @ApiResponse(responseCode = "200", description = "성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class)))
    @PostMapping(value = "/orders/payment")
    public ResponseDto<Void> paymentOrder(@RequestBody @Valid PaymentOrderApiReqDto reqDto){
        orderPaymentFacade.pay(reqDto.buyerId(), reqDto.orderId());
        return ResponseUtil.success();
    }

    @Operation(summary = "주문 조회")
    @ApiResponse(responseCode = "200", description = "성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = FindOrderApiResDto.class)))
    @GetMapping(value = "/orders/{buyerId}")
    public ResponseDto<FindOrderApiResDto> findOrder(@PathVariable(name = "buyerId") @Schema(description = "회원 ID") @NotNull Long buyerId,
                                                     @RequestParam(name = "orderId") @Schema(description = "주문 ID") @NotNull Long orderId){
        return ResponseUtil.success(FindOrderApiResDto.from(orderService.findOrder(buyerId, orderId)));
    }
}
