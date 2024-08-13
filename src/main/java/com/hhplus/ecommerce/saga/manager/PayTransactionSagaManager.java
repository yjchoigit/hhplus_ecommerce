package com.hhplus.ecommerce.saga.manager;


import com.hhplus.ecommerce.domain.payment.event.PaymentEventPublish;
import com.hhplus.ecommerce.domain.payment.event.dto.PayEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PayTransactionSagaManager {
    private final PaymentEventPublish paymentEventPublish;

    // 결제 처리 로직 실행
    public void payProcess(Long buyerId, Long orderId) {
        try {
            // 결제 처리 시작
            paymentEventPublish.pay(new PayEvent(buyerId, orderId));
        } catch(Exception e) {
            // 결제 처리 실패
            paymentEventPublish.payFailed(new PayEvent(buyerId, orderId));
        }
    }
}