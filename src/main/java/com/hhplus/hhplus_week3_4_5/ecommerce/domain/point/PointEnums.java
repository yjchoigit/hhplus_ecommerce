package com.hhplus.hhplus_week3_4_5.ecommerce.domain.point;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class PointEnums {
    @Getter
    @RequiredArgsConstructor
    public enum Type {
        DEDUCT, CHARGE
    }
}