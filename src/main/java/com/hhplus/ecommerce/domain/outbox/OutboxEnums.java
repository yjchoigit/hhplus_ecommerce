package com.hhplus.ecommerce.domain.outbox;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class OutboxEnums {
    @Getter
    @RequiredArgsConstructor
    public enum Status {
        INIT, PUBLISHED, PROCESSED
    }
}
