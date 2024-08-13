package com.hhplus.ecommerce.domain.order.event;

import com.hhplus.ecommerce.domain.order.event.dto.OrderPaymentCompleteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderEventPublish {
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void orderPaymentComplete(OrderPaymentCompleteEvent event){
        eventPublisher.publishEvent(event);
    }
}
