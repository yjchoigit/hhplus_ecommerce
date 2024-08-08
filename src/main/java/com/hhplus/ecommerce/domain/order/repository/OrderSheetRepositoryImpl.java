package com.hhplus.ecommerce.domain.order.repository;

import com.hhplus.ecommerce.domain.order.entity.OrderSheet;
import org.springframework.stereotype.Repository;

@Repository
public class OrderSheetRepositoryImpl implements OrderSheetRepository {
    private final OrderSheetJpaRepository orderSheetJpaRepository;

    public OrderSheetRepositoryImpl(OrderSheetJpaRepository orderSheetJpaRepository) {
        this.orderSheetJpaRepository = orderSheetJpaRepository;
    }

    @Override
    public OrderSheet save(OrderSheet orderSheet) {
        return orderSheetJpaRepository.save(orderSheet);
    }

    @Override
    public void delete(OrderSheet orderSheet) {
        orderSheetJpaRepository.delete(orderSheet);
    }

    @Override
    public OrderSheet findByOrderSheetId(Long orderSheetId) {
        return orderSheetJpaRepository.findById(orderSheetId).orElse(null);
    }

    @Override
    public OrderSheet findByBuyerId(Long buyerId) {
        return orderSheetJpaRepository.findFirstByBuyerId(buyerId);
    }
}