package com.hhplus.ecommerce.event.kafka;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import com.hhplus.ecommerce.base.setting.Setting;
import com.hhplus.ecommerce.domain.buyer.entity.Buyer;
import com.hhplus.ecommerce.domain.order.OrderEnums;
import com.hhplus.ecommerce.domain.order.entity.Order;
import com.hhplus.ecommerce.domain.order.entity.OrderItemSheet;
import com.hhplus.ecommerce.domain.order.entity.OrderSheet;
import com.hhplus.ecommerce.domain.order.repository.OrderItemSheetRepository;
import com.hhplus.ecommerce.domain.outbox.OutboxEnums;
import com.hhplus.ecommerce.domain.outbox.entity.Outbox;
import com.hhplus.ecommerce.domain.payment.entity.Payment;
import com.hhplus.ecommerce.domain.product.entity.Product;
import com.hhplus.ecommerce.domain.product.entity.ProductOption;
import com.hhplus.ecommerce.fixture.buyer.BuyerFixture;
import com.hhplus.ecommerce.fixture.order.OrderFixture;
import com.hhplus.ecommerce.fixture.order.OrderSheetFixture;
import com.hhplus.ecommerce.fixture.point.PointFixture;
import com.hhplus.ecommerce.fixture.product.ProductFixture;
import com.hhplus.ecommerce.service.outbox.OutboxService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EmbeddedKafka(partitions = 1, topics = {KafkaConstants.ORDER_PAYMENT_COMPLETE_TOPIC})
public class KafkaIntegratedTest extends Setting {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private OrderFixture orderFixture;

    @Autowired
    private BuyerFixture buyerFixture;

    @Autowired
    private PointFixture pointFixture;

    @Autowired
    private ProductFixture productFixture;

    @Autowired
    private OrderSheetFixture orderSheetFixture;

    @Autowired
    private OrderItemSheetRepository orderItemSheetRepository;

    private Order order;
    private Payment payment;

    @BeforeEach
    void setUp(){
        Buyer buyer = buyerFixture.add_buyer();
        pointFixture.add_point(buyer.getBuyerId(), 100000);
        OrderSheet orderSheet = orderSheetFixture.add_order_sheet(buyer, 10);

        Product product = productFixture.add_usable_product();
        List<ProductOption> productOptionList = productFixture.add_usable_product_option(product);
        for(ProductOption option : productOptionList){
            productFixture.add_product_stock(product, option, 100);
        }

        for(ProductOption option : productOptionList){
            orderItemSheetRepository.save(new OrderItemSheet(orderSheet, product.getProductId(), product.getName(),
                    option.getProductOptionId(), option.optionStr(),
                    1000, 10, OrderEnums.Status.WAIT));
        }
        order = orderFixture.add_order_wait(orderSheet.getOrderSheetId(), buyer, 10);
        payment = orderFixture.add_payment_wait(order);
    }

    @Test
    @DisplayName("카프카 발행 후 소비 성공")
    public void testKafkaProducerAndConsumer_success() {
        // Producer가 이벤트를 전송
        KafkaProducer kafkaProducer = new KafkaProducer(kafkaTemplate, outboxService);
        kafkaProducer.sendOrderPaymentCompleteEvent(1L, payment);

        // Kafka 메시지가 제대로 전송되었는지 확인
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(KafkaConstants.ORDER_GROUP, "true", embeddedKafkaBroker);
        ConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties(KafkaConstants.ORDER_PAYMENT_COMPLETE_TOPIC);
        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

        container.setupMessageListener((MessageListener<String, String>) record -> {
            // 메시지가 도착하면 Outbox 서비스에서 데이터를 조회하여 검증
            Long outboxId = Long.valueOf(record.value());
            Outbox outbox = outboxService.findById(outboxId);
            assertThat(outbox).isNotNull();
            assertThat(outbox.getRelationId()).isEqualTo(1L);
            assertThat(outbox.getEventType()).isEqualTo(OutboxEnums.EventType.ORDER_PAYMENT_COMPLETE);
        });

        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

        // 테스트 종료 후 컨테이너 정지
        container.stop();
    }

    @Test
    @DisplayName("카프카 발행 실패")
    public void testKafkaProducerAndConsumer_noMessage_fail() throws InterruptedException {
        // Producer가 이벤트를 전송하지만 실패
        KafkaProducer kafkaProducer = new KafkaProducer(kafkaTemplate, outboxService);
        kafkaProducer.sendOrderPaymentCompleteEvent(1L, payment);

        // Kafka 메시지가 제대로 전송되지 않았으므로 Consumer에서 메시지를 받지 못할 것
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(KafkaConstants.ORDER_GROUP, "true", embeddedKafkaBroker);
        ConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties(KafkaConstants.ORDER_PAYMENT_COMPLETE_TOPIC + "_1");
        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

        AtomicBoolean messageReceived = new AtomicBoolean(false);

        container.setupMessageListener((MessageListener<String, String>) record -> {
            // 메시지가 도착 X
            messageReceived.set(true);
        });

        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

        // 메시지가 도착하지 않음을 검증
        long waitTimeMillis = 5000;
        Thread.sleep(waitTimeMillis);

        assertFalse(messageReceived.get());

        // 테스트 종료 후 컨테이너 정지
        container.stop();
    }

    @Test
    @DisplayName("카프카 발행 후 소비 실패 > Outbox 서비스 오류")
    public void testKafkaProducerAndConsumer_outboxServiceError_fail() throws InterruptedException {
        // Producer가 이벤트를 전송
        KafkaProducer kafkaProducer = new KafkaProducer(kafkaTemplate, outboxService);
        kafkaProducer.sendOrderPaymentCompleteEvent(1L, payment);

        // Mock OutboxService > error 발생
        OutboxService mockOutboxService = mock(OutboxService.class);
        when(mockOutboxService.findById(anyLong())).thenThrow(new RuntimeException("OutboxService error"));

        // When
        // Kafka 메시지가 제대로 전송되었으므로 Consumer에서 메시지를 받지만 OutboxService에서 오류 발생
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(KafkaConstants.ORDER_GROUP, "true", embeddedKafkaBroker);
        ConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        ContainerProperties containerProperties = new ContainerProperties(KafkaConstants.ORDER_PAYMENT_COMPLETE_TOPIC);
        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

        AtomicBoolean processingFailed = new AtomicBoolean(false);

        container.setupMessageListener((MessageListener<String, String>) record -> {
            try {
                Long outboxId = Long.valueOf(record.value());
                Outbox outbox = mockOutboxService.findById(outboxId);
                processingFailed.set(true);
            } catch (Exception e) {
                processingFailed.set(true);
            }
        });

        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

        long waitTimeMillis = 5000;
        Thread.sleep(waitTimeMillis);

        assertTrue(processingFailed.get(), "OutboxService 오류로 인해 메시지 처리가 실패했습니다.");

        // 테스트 종료 후 컨테이너 정지
        container.stop();
    }

}
