package com.hhplus.ecommerce.domain.order.repository;

import com.hhplus.ecommerce.domain.order.entity.Order;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepositoryImpl implements OrderRepository {
    private OrderJpaRepository orderJpaRepository;

    public OrderRepositoryImpl(OrderJpaRepository orderJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
    }

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Order findByBuyerIdAndOrderId(Long buyerId, Long orderId) {
        return orderJpaRepository.findByBuyerIdAndOrderId(buyerId, orderId);
    }

    @Override
    public Order findByOrderId(Long orderId) {
        return orderJpaRepository.findByOrderId(orderId);
    }
}
