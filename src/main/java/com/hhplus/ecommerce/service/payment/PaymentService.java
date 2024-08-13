package com.hhplus.ecommerce.service.payment;

import com.hhplus.ecommerce.domain.payment.entity.Payment;
import org.springframework.transaction.annotation.Transactional;

public interface PaymentService {
    // 결제 처리
    Payment pay(Long buyerId, Long orderId);
    
    // 결제 처리 -> 대기 상태로 다시 업데이트
    void revertPay(Long orderId);
}
