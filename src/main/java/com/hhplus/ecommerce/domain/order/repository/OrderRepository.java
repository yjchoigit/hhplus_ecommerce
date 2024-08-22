package com.hhplus.ecommerce.domain.order.repository;

import com.hhplus.ecommerce.domain.order.entity.Order;

public interface OrderRepository {
    Order save(Order order);
    Order findByBuyerIdAndOrderId(Long buyerId, Long orderId);
    Order findByOrderId(Long orderId);
}
