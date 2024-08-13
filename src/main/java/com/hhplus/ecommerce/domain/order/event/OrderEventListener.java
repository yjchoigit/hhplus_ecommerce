package com.hhplus.ecommerce.domain.order.event;

import com.hhplus.ecommerce.domain.order.entity.Order;
import com.hhplus.ecommerce.domain.order.event.dto.OrderPaymentCompleteEvent;
import com.hhplus.ecommerce.domain.payment.entity.Payment;
import com.hhplus.ecommerce.infrastructure.apiClient.order.OrderCollectApiClient;
import com.hhplus.ecommerce.infrastructure.apiClient.order.dto.SendOrderToCollectionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderEventListener {

    private final OrderCollectApiClient orderCollectApiClient;

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sentOrderPaymentInfo(OrderPaymentCompleteEvent event){
        Payment payment = event.payment();
        Order order = payment.getOrder();
        try {
            log.info("주문 데이터 수집 >> 외부 데이터 플랫폼 전달 실행");
            orderCollectApiClient.sendOrderToCollectionPlatform(SendOrderToCollectionDto.builder()
                    .orderNumber(order.getOrderNumber())
                    .buyerName(order.getBuyerName())
                    .paymentPrice(payment.getPaymentPrice())
                    .paymentId(payment.getPaymentId())
                    .paymentCreateDatetime(payment.getCreateDatetime())
                    .build());
        } catch (Exception e) {
            log.error("주문 데이터 수집 >> 외부 데이터 플랫폼 전달 실패 : {}", e.getMessage());
        }
    }

}
