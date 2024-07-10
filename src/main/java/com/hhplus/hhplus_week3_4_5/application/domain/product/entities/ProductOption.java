package com.hhplus.hhplus_week3_4_5.application.domain.product.entities;

import com.hhplus.hhplus_week3_4_5.application.domain.base.entities.CreateModifyDateTimeEntity;
import com.hhplus.hhplus_week3_4_5.application.domain.product.ProductEnums;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ProductOption extends CreateModifyDateTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("상품 옵션 id")
    private Long productOptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @Comment("상품 id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("상품 옵션 타입 Enum")
    private ProductEnums.OptionType type;

    @Column(nullable = false)
    @Comment("옵션 명")
    private String optionName;

    @Column(nullable = false)
    @Comment("옵션 값")
    private String optionValue;

    @Column(nullable = false)
    @Comment("옵션 가격")
    private BigDecimal price;

    @Column(nullable = false, columnDefinition = "char(1)")
    @Comment("사용 여부")
    private boolean useYn;

    @Builder
    public ProductOption(Long productOptionId, Product product, ProductEnums.OptionType type, String optionName, String optionValue, BigDecimal price, boolean useYn) {
        this.productOptionId = productOptionId;
        this.product = product;
        this.type = type;
        this.optionName = optionName;
        this.optionValue = optionValue;
        this.price = price;
        this.useYn = useYn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductOption that = (ProductOption) o;
        return Objects.equals(getProductOptionId(), that.getProductOptionId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getProductOptionId());
    }
}