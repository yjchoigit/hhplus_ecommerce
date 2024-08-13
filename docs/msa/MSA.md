서비스 확장에 대응하는 Event + SAGA 패턴 
===========================
*****
AS-IS
--------
- 이커머스 구축 프로젝트를 진행하면서 결제처리 비즈니스 로직을 향상시키기 위해 아래와 같은 기술과 패턴을 사용했다.
  - 분산락 (RLock, redissonClient)
  - 예외처리, 롤백 (@Transactional)
  - 로그 기록 (@Slf4j)
    ```java
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
            payProcess(buyerId, orderDto);
    
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
            // 결제 처리 -> 결제상태 완료로 업데이트 처리
            Payment payment = orderPaymentService.pay(buyerId, orderDto.orderId());
            // 잔액 사용처리 (잔액 valid, 잔액 사용처리)
            pointService.usePoint(buyerId, orderDto.totalPrice());
    
            // 주문데이터 외부 플랫폼 전달
            orderClient.sendOrderFlatform();
        }
    ```
- 그렇지만 해당 로직을 행하는 서비스의 규모가 커지거나 분리해야할 때에 대한 대비를 반영한 로직이라고는 생각하지 않는다.
  - pay() 와 payProcess() 로 분리하였지만 payProcess() 안의 서비스를 한번에 실행하고 있기 때문에 중간에 오류가 발생할 시에 대한 대처가 미흡하다고 생각했다.

*****
TO-BE-1
--------
- 위의 이슈를 해결하고자 이벤트 객체,발행,구독을 사용했다.
- Publisher
  ```java
  @Slf4j
  @RequiredArgsConstructor
  @Service
  public class OrderEventPublish {
  private final ApplicationEventPublisher eventPublisher;
    
      @Transactional
      public void orderPaymentComplete(OrderPaymentCompleteEvent event){
          eventPublisher.publishEvent(event);
      }
    }
  ``` 
- Event
  ```java
      public record OrderPaymentCompleteEvent(
              Long buyerId,
              Payment payment
      ) {
      }
  ``` 
- Listener
    ```java
      @Slf4j
      @RequiredArgsConstructor
      @Service
      public class OrderEventListener {
        
          private final OrderCollectApiClient orderCollectApiClient;
        
          @Async("taskExecutor")
          @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
          public void sentOrderPaymentInfo(OrderPaymentCompleteEvent event){
              Payment payment = event.payment();
              Order order = payment.getOrder();
              try {
                  log.info("주문 데이터 수집 >> 외부 데이터 플랫폼 전달 실행");
                  orderCollectApiClient.sendOrderToCollectionPlatform(SendOrderToCollectionDto.builder()
                          .orderNumber(order.getOrderNumber())
                          .buyerName(order.getBuyerName())
                          .paymentPrice(payment.getPaymentPrice())
                          .paymentId(payment.getPaymentId())
                          .paymentCreateDatetime(payment.getCreateDatetime())
                          .build());
              } catch (Exception e) {
                  log.error("주문 데이터 수집 >> 외부 데이터 플랫폼 전달 실패 : {}", e.getMessage());
              }
          }
      }
    ``` 
    - @Async : 비동기로 처리
    - @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) : 트랜잭션이 완료 후 실행되게 함 
      - 외부 데이터 플랫폼 전달은 성공 or 실패 발생해도 상관없다고 생각했다.
  - 이벤트 => 결제 처리 비즈니스 로직에 반영
    ```java
      @Transactional(rollbackFor = Exception.class)
      public void payProcess(Long buyerId, FindOrderResDto orderDto){
          // 결제 처리 -> 결제상태 완료로 업데이트 처리
          Payment payment = orderPaymentService.pay(buyerId, orderDto.orderId());
          // 잔액 사용처리 (잔액 valid, 잔액 사용처리)
          pointService.usePoint(buyerId, orderDto.totalPrice());
    
          // 주문결제 완료 이벤트 발행 (주문데이터 외부 플랫폼 전달)
          orderEventPublish.orderPaymentComplete(new OrderPaymentCompleteEvent(buyerId, payment));
      }
    ```
    - 주문데이터 외부 플랫폼 전달 로직을 이벤트로 발행했다.
      - 시스템 간 비동기적 통신을 가능하게 해줌 => 비즈니스 로직의 주요 흐름과 별개로 추가 작업을 처리할 수 있음
      - 메인 프로세스가 느려지지 않고 시스템 부하를 분산시킬 수 있음
      - 동일한 이벤트를 재사용할 수 있으며 기능적으로 분리 -> 관리가 편함
    - 하지만 orderPaymentService.pay(), pointService.usePoint() 내부 로직에서 에러가 발생 시 중간에 커밋되는 상황이 발생할 수 있음
      - 보상 트랜잭션, SAGA 패턴을 이용해 논리적으로 처리를 해야겠다고 생각했다.
*****
TO-BE-2
--------
- 위의 이슈를 해결하고자 payProcess() 내부에 사용되는 서비스를 모두 이벤트로 나눴고 SAGA 패턴으로 처리하였다.
- SAGA 패턴
  - 마이크로서비스들끼리 이벤트를 주고 받아 특정 마이크로서비스에서의 작업이 실패하면 이전까지의 작업이 완료된 서비스에게 보상해주는 패턴
    - Orchestration based SAGA pattern 기반으로 구현하였다.
      - 서비스 간 복잡성이 줄어들고 트랜잭션의 현재 상태를 알 수 있음
  - SAGA 패턴을 아래와 같은 순서로 적용시킴 (A -> B -> C 순서)
  ```
  - 결제상태 업데이트 로직 실행 시 (A)
      - 성공 (A-1)
          - 잔액처리 이벤트 실행 (B)
              - 성공 (B-1)
                  - 주문 데이터 외부 플랫폼 전달 (C)
                      - 성공 (C-1)
                          - 주문결제 비즈니로직 성공 마무리
                      - 실패 (C-2)
                          - 외부 플랫폼 전달 부분만 실패했으므로 다른 로직에 대한 롤백은 없이 마무리
              - 실패 (B-2)
                  - 결제상태 이전 상태로 원복
                  - 사용한 잔액 다시 돌려주기
      - 실패 (A-2)
          - 결제상태 이전 상태로 원복   
  ``` 
- PayTransactionSagaManager
  ```java
    @Component
    @RequiredArgsConstructor
    @Slf4j
    public class PayTransactionSagaManager {
    private final PaymentEventPublish paymentEventPublish;
    
        // 결제 처리 로직 실행
        public void payProcess(Long buyerId, Long orderId) {
            try {
                // 결제 처리 시작
                paymentEventPublish.pay(new PayEvent(buyerId, orderId));
            } catch(Exception e) {
                // 결제 처리 실패
                paymentEventPublish.payFailed(new PayEvent(buyerId, orderId));
            }
        }
    }
  ```
- PaymentEventListener (+ PointEventListener, OrderEventListener)
  ```java
    @Slf4j
    @RequiredArgsConstructor
    @Service
    public class PaymentEventListener {
    
        private final PaymentService paymentService;
        private final PointEventPublish pointEventPublish;
        private final EventStatusManager eventStatusManager;
    
        @Async("taskExecutor")
        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void pay(PayEvent event){
            String eventId = String.valueOf(event.buyerId()); // 이벤트 고유 식별자
            try {
                if (eventStatusManager.isEventProcessed(eventId)) {
                    log.info("이벤트 {}는 이미 처리되었습니다.", eventId);
                    return;
                }
    
                log.info("결제처리 > 결제상태 결제완료로 업데이트 실행");
                Payment payment = paymentService.pay(event.buyerId(), event.orderId());
    
                if(payment != null){
                    // 잔액 사용처리 실행
                    pointEventPublish.usePoint(new UsePointEvent(event.buyerId(), payment));
                    eventStatusManager.markEventAsProcessed(eventId); // 이벤트 처리 완료 표시
                }
    
            } catch (Exception e) {
                log.error("결제처리 중 오류 발생: ", e);
                // 결제 처리 실패 이벤트 발행
                payFailed(event);
            }
        }
    
        @Async("taskExecutor")
        @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
        public void payFailed(PayEvent event){
            log.info("결제처리 실패 -> 원래 결제상태로 업데이트 처리");
            paymentService.revertPay(event.buyerId());
            eventStatusManager.markEventAsProcessed(String.valueOf(event.buyerId())); // 실패 처리 후에도 이벤트 상태 업데이트
        }
    }
  ```
  - PayTransactionSagaManager로 처음 불러와서 PaymentEventListener, PointEventListener, OrderEventListener를 이용하여 각각의 서비스를 이벤트로 발행하여 처리하였다.
    - 도중에 실패 or 에러가 발생하는 경우 FailEvent()를 실행시켜 보상 트랜잭션을 진행하였다.

  - PayTransactionSagaManager => 결제 처리 비즈니스 로직에 반영
    ```java
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
              //payProcess(buyerId, orderDto);
              payTransactionSagaManager.payProcess(buyerId, orderId);
             .....
    ```
    - 기존의 payProcess() 대신에 payTransactionSagaManager.payProcess()를 불러온다.
      - 비즈니스 프로세스를 여러 독립적인 단계로 분리할 수 있으며, 각 단계가 실패했을 때 롤백할 수 있는 유연성을 제공함
      - 각 서비스가 독립적으로 동작
      - 시스템의 유연성, 확장성을 높여주고 장애 발생 시 대응하기에 편함