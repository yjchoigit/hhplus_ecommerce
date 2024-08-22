package com.hhplus.ecommerce.scheduler.outbox;

import com.hhplus.ecommerce.base.setting.Setting;
import com.hhplus.ecommerce.domain.outbox.OutboxEnums;
import com.hhplus.ecommerce.domain.outbox.entity.Outbox;
import com.hhplus.ecommerce.event.kafka.KafkaConstants;
import com.hhplus.ecommerce.fixture.outbox.OutboxFixture;
import com.hhplus.ecommerce.service.outbox.OutboxService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@EmbeddedKafka(partitions = 1, topics = {KafkaConstants.ORDER_PAYMENT_COMPLETE_TOPIC})
class OutboxEventSchedulerTest extends Setting {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private OutboxService outboxService;

    @Autowired
    private OutboxEventProcessor outboxEventProcessor;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private OutboxFixture outboxFixture;

    @Test
    @DisplayName("Outbox 이벤트 스케줄러 성공")
    public void testProcessOutboxEvents_success() throws InterruptedException {
        // given
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);

        Outbox outbox1 = outboxFixture.add_outbox(tenMinutesAgo.minusMinutes(1)); // 9분 전
        Outbox outbox2 = outboxFixture.add_outbox(tenMinutesAgo.plusMinutes(1)); // 11분 전

        // when
        outboxEventProcessor.processOutboxEvents();

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        ConsumerFactory<String, String> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(consumerFactory, new ContainerProperties(KafkaConstants.ORDER_PAYMENT_COMPLETE_TOPIC));

        AtomicBoolean messageReceived = new AtomicBoolean(false);

        container.setupMessageListener((MessageListener<String, String>) record -> {
            Long outboxId = Long.valueOf(record.value());
            Outbox outbox = outboxService.findById(outboxId);
            if (outbox != null && outbox.getStatus() == OutboxEnums.Status.PUBLISHED) {
                messageReceived.set(true);
            }
        });

        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

        long waitTimeMillis = 5000;
        Thread.sleep(waitTimeMillis);

        Outbox updatedOutbox1 = outboxService.findById(outbox1.getOutboxId());
        assertThat(updatedOutbox1.getStatus()).isEqualTo(OutboxEnums.Status.PUBLISHED);

        Outbox updatedOutbox2 = outboxService.findById(outbox2.getOutboxId());
        assertThat(updatedOutbox2.getStatus()).isEqualTo(OutboxEnums.Status.INIT);

        assertTrue(messageReceived.get(), "Kafka 메시지가 수신되지 않았습니다.");

        // 테스트 종료 후 컨테이너 정지
        container.stop();
    }
}
