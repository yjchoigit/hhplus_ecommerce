package com.hhplus.ecommerce.facade.order;

import com.hhplus.ecommerce.base.config.redis.RedisCustomException;
import com.hhplus.ecommerce.base.config.redis.RedisEnums;
import com.hhplus.ecommerce.controller.order.dto.CreateOrderApiReqDto;
import com.hhplus.ecommerce.domain.order.entity.Order;
import com.hhplus.ecommerce.domain.order.event.OrderEventPublish;
import com.hhplus.ecommerce.domain.order.event.dto.OrderPaymentCompleteEvent;
import com.hhplus.ecommerce.domain.payment.entity.Payment;
import com.hhplus.ecommerce.domain.payment.event.PaymentEventPublish;
import com.hhplus.ecommerce.saga.manager.PayTransactionSagaManager;
import com.hhplus.ecommerce.service.payment.PaymentService;
import com.hhplus.ecommerce.service.order.OrderService;
import com.hhplus.ecommerce.service.order.OrderSheetService;
import com.hhplus.ecommerce.service.order.dto.FindOrderResDto;
import com.hhplus.ecommerce.service.point.PointService;
import com.hhplus.ecommerce.service.product.ProductService;
import com.hhplus.ecommerce.service.product.ProductStockService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Component
@AllArgsConstructor
@Slf4j
public class OrderPaymentFacade {
    private OrderSheetService orderSheetService;
    private OrderService orderService;
    private PointService pointService;
    private ProductService productService;
    private ProductStockService productStockService;
    private PaymentService orderPaymentService;
    private OrderEventPublish orderEventPublish;
    private RedissonClient redissonClient;
    private PaymentEventPublish paymentEventPublish;
    private PayTransactionSagaManager payTransactionSagaManager;

    // 주문 생성
    public Long createOrder(CreateOrderApiReqDto reqDto){
        // 주문서 id 기준으로 Lock 객체를 가져옴
        RLock rLock = redissonClient.getLock(RedisEnums.LockName.CREATE_ORDER.changeLockName(reqDto.orderSheetId()));
        boolean isLocked = false;

        try {
            // 락의 이름으로 RLock 인스턴스 가져옴
            log.info("try lock: {}", rLock.getName());
            isLocked = rLock.tryLock(0,  3, TimeUnit.SECONDS);

            // 락을 획득하지 못했을 떄
            if (!isLocked) {
                throw new RedisCustomException(RedisEnums.Error.LOCK_NOT_ACQUIRE);
            }

            // 트랜잭션 내에서 실행할 메서드 호출
            return createOrderProcess(reqDto);
        } catch (InterruptedException e) {
            throw new RedisCustomException(RedisEnums.Error.LOCK_INTERRUPTED_ERROR);
        } finally {
            if (isLocked) {
                try{
                    if (rLock.isHeldByCurrentThread()) {
                        rLock.unlock();
                        log.info("unlock complete: {}", rLock.getName());
                    }
                }catch (IllegalMonitorStateException e){
                    //이미 종료된 락일 때 발생하는 예외
                    throw new RedisCustomException(RedisEnums.Error.UNLOCKING_A_LOCK_WHICH_IS_NOT_LOCKED);
                }
            }
        }
    }

    @Transactional(rollbackFor = {Exception.class})
    public Long createOrderProcess(CreateOrderApiReqDto reqDto) {
        // 상품 프로세스 진행 (상품 valid)
        productProcess(reqDto);
        // 재고 프로세스 진행 (재고 valid, 재고 차감처리)
        stockProcess(reqDto);

        // 주문 생성 진행
        Order order = orderService.createOrder(reqDto);
        // 주문 정보 조회
        FindOrderResDto orderDto = orderService.findOrder(reqDto.buyerId(), order.getOrderId());

        // 주문서 삭제 처리
        orderSheetService.delOrderSheet(reqDto.orderSheetId());

        return orderDto.orderId();
    }

    private void productProcess(CreateOrderApiReqDto reqDto){
        for(CreateOrderApiReqDto.CreateOrderItemApiReqDto item : reqDto.orderItemList()){
            Long productId = item.productId();
            Long productOptionId = item.productOptionId();
            
            // 상품 정보 조회
            productService.findProductByProductId(productId);
            
            // 상품 옵션 id 존재 시 상품 옵션 정보 조회
            if(productOptionId != null){
               productService.findProductOptionByProductIdAndProductOptionId(productId, productOptionId);
            }
        }
    }

    private void stockProcess(CreateOrderApiReqDto reqDto){
        for(CreateOrderApiReqDto.CreateOrderItemApiReqDto item : reqDto.orderItemList()) {
            Long productId = item.productId();
            Long productOptionId = item.productOptionId();
            int buyCnt = item.buyCnt();

            productStockService.deductProductStock(productId, productOptionId, buyCnt);
        }
    }


    // 결제 처리
    public void pay(Long buyerId, Long orderId) {
        // 주문 id 기준으로 Lock 객체를 가져옴
        RLock rLock = redissonClient.getLock(RedisEnums.LockName.PAYMENT_ORDER.changeLockName(orderId));
        boolean isLocked = false;

        try {
            // 락의 이름으로 RLock 인스턴스 가져옴
            log.info("try lock: {}", rLock.getName());
            isLocked = rLock.tryLock(0,  3, TimeUnit.SECONDS);

            // 락을 획득하지 못했을 떄
            if (!isLocked) {
                throw new RedisCustomException(RedisEnums.Error.LOCK_NOT_ACQUIRE);
            }

            // 주문 정보 조회
            FindOrderResDto orderDto = orderService.findOrder(buyerId, orderId);

            // 트랜잭션 내의 비즈니스 로직 수행
            payTransactionSagaManager.payProcess(buyerId, orderId);

        } catch (InterruptedException e) {
            throw new RedisCustomException(RedisEnums.Error.LOCK_INTERRUPTED_ERROR);
        } finally {
            if (isLocked) {
                try{
                    if (rLock.isHeldByCurrentThread()) {
                        rLock.unlock();
                        log.info("unlock complete: {}", rLock.getName());
                    }
                }catch (IllegalMonitorStateException e){
                    //이미 종료된 락일 때 발생하는 예외
                    throw new RedisCustomException(RedisEnums.Error.UNLOCKING_A_LOCK_WHICH_IS_NOT_LOCKED);
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void payProcess(Long buyerId, FindOrderResDto orderDto){
        // 잔액 사용처리 (잔액 valid, 잔액 사용처리)
//        pointService.usePoint(buyerId, orderDto.totalPrice());
        // 결제 처리 -> orderPaymentId 반환
        Payment payment = orderPaymentService.pay(buyerId, orderDto.orderId());

        // 주문결제 완료 이벤트 발행 (잔액 사용처리, 주문데이터 외부 플랫폼 전달)
        orderEventPublish.orderPaymentComplete(new OrderPaymentCompleteEvent(buyerId, payment));
    }

}
