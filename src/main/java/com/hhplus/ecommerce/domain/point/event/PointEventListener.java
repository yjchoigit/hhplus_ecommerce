package com.hhplus.ecommerce.domain.point.event;

import com.hhplus.ecommerce.domain.order.event.OrderEventPublish;
import com.hhplus.ecommerce.domain.order.event.dto.OrderPaymentCompleteEvent;
import com.hhplus.ecommerce.domain.point.entity.Point;
import com.hhplus.ecommerce.domain.point.event.dto.UsePointEvent;
import com.hhplus.ecommerce.saga.manager.EventStatusManager;
import com.hhplus.ecommerce.service.point.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Service
public class PointEventListener {
    private final PointService pointService;
    private final OrderEventPublish orderEventPublish;
    private final EventStatusManager eventStatusManager;

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void usePoint(UsePointEvent event){
        String eventId = String.valueOf(event.buyerId()); // 이벤트 고유 식별자

        try {
            if (eventStatusManager.isEventProcessed(eventId)) {
                log.info("이벤트 {}는 이미 처리되었습니다.", eventId);
                return;
            }
            log.info("잔액 사용처리 실행");
            Point point = pointService.usePoint(event.buyerId(), event.payment().getPaymentPrice());

            if(point != null){
                // 주문 데이터 외부 플랫폼 전달
                orderEventPublish.orderPaymentComplete(new OrderPaymentCompleteEvent(event.buyerId(), event.payment()));
                eventStatusManager.markEventAsProcessed(eventId); // 이벤트 처리 완료 표시
            }

        } catch (Exception e) {
            log.error("잔액 사용처리 중 오류 발생: ", e);
            // 잔액 사용처리 실패 이벤트 발행
            usePointFailed(event);
        }

    }

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void usePointFailed(UsePointEvent event){
        log.info("잔액 사용처리 실패 -> 다시 잔액 돌려주기");
        pointService.revertPointUsage(event.buyerId(), event.payment().getPaymentPrice());
        eventStatusManager.markEventAsProcessed(String.valueOf(event.buyerId())); // 이벤트 처리 완료 표시
    }
}
