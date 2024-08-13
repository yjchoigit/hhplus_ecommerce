
package com.hhplus.ecommerce.domain.payment.event;

import com.hhplus.ecommerce.domain.payment.entity.Payment;
import com.hhplus.ecommerce.domain.payment.event.dto.PayEvent;
import com.hhplus.ecommerce.domain.point.event.dto.PointEventPublish;
import com.hhplus.ecommerce.domain.point.event.dto.UsePointEvent;
import com.hhplus.ecommerce.saga.manager.EventStatusManager;
import com.hhplus.ecommerce.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentEventListener {

    private final PaymentService paymentService;
    private final PointEventPublish pointEventPublish;
    private final EventStatusManager eventStatusManager;

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void pay(PayEvent event){
        String eventId = String.valueOf(event.buyerId()); // 이벤트 고유 식별자
        try {
            if (eventStatusManager.isEventProcessed(eventId)) {
                log.info("이벤트 {}는 이미 처리되었습니다.", eventId);
                return;
            }

            log.info("결제처리 > 결제상태 결제완료로 업데이트 실행");
            Payment payment = paymentService.pay(event.buyerId(), event.orderId());

            if(payment != null){
                // 잔액 사용처리 실행
                pointEventPublish.usePoint(new UsePointEvent(event.buyerId(), payment));
                eventStatusManager.markEventAsProcessed(eventId); // 이벤트 처리 완료 표시
            }

        } catch (Exception e) {
            log.error("결제처리 중 오류 발생: ", e);
            // 결제 처리 실패 이벤트 발행
            payFailed(event);
        }
    }

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void payFailed(PayEvent event){
        log.info("결제처리 실패 -> 원래 결제상태로 업데이트 처리");
        paymentService.revertPay(event.buyerId());
        eventStatusManager.markEventAsProcessed(String.valueOf(event.buyerId())); // 실패 처리 후에도 이벤트 상태 업데이트
    }

}
