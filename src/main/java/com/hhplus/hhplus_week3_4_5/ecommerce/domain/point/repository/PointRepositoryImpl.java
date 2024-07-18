package com.hhplus.hhplus_week3_4_5.ecommerce.domain.point.repository;

import com.hhplus.hhplus_week3_4_5.ecommerce.domain.point.entity.Point;
import com.hhplus.hhplus_week3_4_5.ecommerce.domain.product.ProductEnums;
import com.hhplus.hhplus_week3_4_5.ecommerce.domain.product.exception.ProductCustomException;
import org.springframework.stereotype.Repository;

@Repository
public class PointRepositoryImpl implements PointRepository {

    private final PointJpaRepository pointJpaRepository;

    public PointRepositoryImpl(PointJpaRepository pointJpaRepository) {
        this.pointJpaRepository = pointJpaRepository;
    }

    @Override
    public Point findByBuyerId(Long buyerId) {
        Point point = pointJpaRepository.findByBuyerId(buyerId);
        if(point == null) {
            throw new ProductCustomException(ProductEnums.Error.DELETE_PRODUCT);
        }
        return point;
    }

    @Override
    public Point save(Point point) {
        return pointJpaRepository.save(point);
    }
}
