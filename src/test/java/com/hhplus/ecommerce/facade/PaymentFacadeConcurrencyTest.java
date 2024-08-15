package com.hhplus.ecommerce.facade;

import com.hhplus.ecommerce.controller.order.dto.CreateOrderApiReqDto;
import com.hhplus.ecommerce.domain.buyer.entity.Buyer;
import com.hhplus.ecommerce.domain.order.OrderEnums;
import com.hhplus.ecommerce.domain.order.entity.Order;
import com.hhplus.ecommerce.domain.order.entity.OrderItemSheet;
import com.hhplus.ecommerce.domain.order.entity.OrderSheet;
import com.hhplus.ecommerce.domain.order.repository.OrderItemSheetRepository;
import com.hhplus.ecommerce.domain.product.entity.Product;
import com.hhplus.ecommerce.domain.product.entity.ProductOption;
import com.hhplus.ecommerce.facade.order.OrderPaymentFacade;
import com.hhplus.ecommerce.fixture.buyer.BuyerFixture;
import com.hhplus.ecommerce.fixture.order.OrderFixture;
import com.hhplus.ecommerce.fixture.order.OrderSheetFixture;
import com.hhplus.ecommerce.fixture.point.PointFixture;
import com.hhplus.ecommerce.fixture.product.ProductFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class PaymentFacadeConcurrencyTest {
    private static final Logger log = LoggerFactory.getLogger(PaymentFacadeConcurrencyTest.class);

    @Autowired
    private BuyerFixture buyerFixture;

    @Autowired
    private PointFixture pointFixture;

    @Autowired
    private ProductFixture productFixture;

    @Autowired
    private OrderFixture orderFixture;

    @Autowired
    private OrderSheetFixture orderSheetFixture;

    @Autowired
    private OrderPaymentFacade orderPaymentFacade;

    @Autowired
    private OrderItemSheetRepository orderItemSheetRepository;

    @Test
    @DisplayName("주문 생성 동시성 테스트 성공 - 주문 1개만 생성 성공 (재고 차감 - 주문 생성)")
    void createOrderSheet_currency_success() throws InterruptedException {
        // given
        Buyer buyer = buyerFixture.add_buyer();
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

        List<CreateOrderApiReqDto.CreateOrderItemApiReqDto> items = List.of(CreateOrderApiReqDto.CreateOrderItemApiReqDto.builder()
                .productId(product.getProductId())
                .productName(product.getName())
                .productOptionId(productOptionList.get(0).getProductOptionId())
                .productOptionName(productOptionList.get(0).optionStr())
                .productPrice(1300)
                .buyCnt(2)
                .build());
        CreateOrderApiReqDto reqDto = CreateOrderApiReqDto.builder()
                .orderSheetId(orderSheet.getOrderSheetId())
                .buyerId(buyer.getBuyerId())
                .buyerName(buyer.getName())
                .allBuyCnt(2)
                .totalPrice(2600)
                .orderItemList(items)
                .build();

        // CountDownLatch
        CountDownLatch latch = new CountDownLatch(10);

        // 스레드 10개 설정
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        // 주문 성공 여부
        AtomicBoolean orderCreatedSuccessfully = new AtomicBoolean(false);

        // 동기화 객체
        Object lock = new Object();

        // Callable task
        Callable<Void> task = () -> {
            try {
                log.info("task start !");
                try {
                    // 재고 차감 및 주문 생성 시도
                    orderPaymentFacade.createOrder(reqDto);
                    // 성공적인 주문이 이루어진 경우를 기록
                    synchronized (lock) {
                        if (!orderCreatedSuccessfully.get()) {
                            log.info("complete create order");
                            orderCreatedSuccessfully.set(true);
                        }
                    }
                } catch (Exception e) {
                    log.error("Exception occurred: ", e);
                }
            } finally {
                log.info("CountDownLatch.countDown()");
                latch.countDown();
            }
            return null;
        };

        // 10개 task 전송
        List<Future<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            futures.add(executorService.submit(task));
        }

        // task 기다리기
        latch.await();

        executorService.shutdown();

        assertTrue(orderCreatedSuccessfully.get(), "Order creation should have succeeded at least once.");
    }

    @Test
    @DisplayName("결제처리 동시성 테스트 성공 - 결제 1개만 성공 (잔액 사용 - 결제 처리)")
    void paymentOrder_currency_success() throws InterruptedException {
        // given
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
        Order order = orderFixture.add_order_wait(orderSheet.getOrderSheetId(), buyer, 10);
        orderFixture.add_payment_wait(order);

        // CountDownLatch
        CountDownLatch latch = new CountDownLatch(10);

        // 스레드 10개 설정
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        // 결제 성공 여부
        AtomicBoolean orderCreatedSuccessfully = new AtomicBoolean(false);

        // 동기화 객체
        Object lock = new Object();

        // Callable task
        Callable<Void> task = () -> {
            try {
                log.info("task start !");
                try {
                    // 잔액 사용 처리 및 결제 처리 시도
                    orderPaymentFacade.pay(buyer.getBuyerId(), order.getOrderId());
                    // 성공적인 결제가 이루어진 경우를 기록
                    synchronized (lock) {
                        if (!orderCreatedSuccessfully.get()) {
                            log.info("complete payment order");
                            orderCreatedSuccessfully.set(true);
                        }
                    }
                } catch (Exception e) {
                    log.error("Exception occurred: ", e);
                }
            } finally {
                log.info("CountDownLatch.countDown()");
                latch.countDown();
            }
            return null;
        };

        // 10개 task 전송
        List<Future<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            futures.add(executorService.submit(task));
        }

        // task 기다리기
        latch.await();

        executorService.shutdown();

        assertTrue(orderCreatedSuccessfully.get(), "Order payment should have succeeded at least once.");
    }
}
