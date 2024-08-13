package com.hhplus.ecommerce.domain.payment.event;

import com.hhplus.ecommerce.domain.order.event.dto.OrderPaymentCompleteEvent;
import com.hhplus.ecommerce.domain.payment.event.dto.PayEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PaymentEventPublish {
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void pay(PayEvent event){
       eventPublisher.publishEvent(event);
    }

    @Transactional
    public void payFailed(PayEvent event){
        eventPublisher.publishEvent(event);
    }
}
