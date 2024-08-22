package com.hhplus.ecommerce.event.kafka;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KafkaConstants {
    public static final String ORDER_PAYMENT_COMPLETE_TOPIC = "order_payment_complete_topic";
    public static final String ORDER_GROUP = "order-group";
}
