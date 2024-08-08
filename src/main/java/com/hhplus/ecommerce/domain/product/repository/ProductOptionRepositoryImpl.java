package com.hhplus.ecommerce.domain.product.repository;

import com.hhplus.ecommerce.domain.product.entity.ProductOption;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductOptionRepositoryImpl implements ProductOptionRepository {
    private final ProductOptionJpaRepository productOptionJpaRepository;

    public ProductOptionRepositoryImpl(ProductOptionJpaRepository productOptionJpaRepository) {
        this.productOptionJpaRepository = productOptionJpaRepository;
    }

    @Override
    public List<ProductOption> findByProductId(Long productId) {
        return productOptionJpaRepository.findAllByProduct_productId(productId);
    }

    @Override
    public ProductOption findProductOptionByProductIdAndProductOptionId(Long productId, Long productOptionId) {
        return productOptionJpaRepository.findProductOptionByProduct_productIdAndProductOptionId(productId, productOptionId);
    }

    @Override
    public ProductOption save(ProductOption productOption) {
        return productOptionJpaRepository.save(productOption);
    }
}