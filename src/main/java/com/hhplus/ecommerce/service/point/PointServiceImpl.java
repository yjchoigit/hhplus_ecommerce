package com.hhplus.ecommerce.service.point;

import com.hhplus.ecommerce.base.config.redis.RedisCustomException;
import com.hhplus.ecommerce.base.config.redis.RedisEnums;
import com.hhplus.ecommerce.controller.point.dto.FindPointHistoryApiResDto;
import com.hhplus.ecommerce.domain.point.PointEnums;
import com.hhplus.ecommerce.domain.point.entity.Point;
import com.hhplus.ecommerce.domain.point.entity.PointHistory;
import com.hhplus.ecommerce.domain.point.exception.PointCustomException;
import com.hhplus.ecommerce.domain.point.repository.PointHistoryRepository;
import com.hhplus.ecommerce.domain.point.repository.PointRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
@Transactional(rollbackFor = {Exception.class}, readOnly = true)
@Slf4j
public class PointServiceImpl implements PointService {
    private PointRepository pointRepository;
    private PointHistoryRepository pointHistoryRepository;
    private RedissonClient redissonClient;

    // 잔액 조회
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Point findPoint(Long buyerId){
        // 회원 id로 잔액 정보 조회
        // 총 포인트 반환
        return pointRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new PointCustomException(PointEnums.Error.NO_POINT));
    }

    // 잔액 충전
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Point chargePoint(Long buyerId, int point){
        // buyerId 기준으로 Lock 객체를 가져옴
        RLock rLock = redissonClient.getLock(RedisEnums.LockName.CHARGE_POINT.changeLockName(buyerId));
        boolean isLocked = false;

        try {
            // 락의 이름으로 RLock 인스턴스 가져옴
            log.info("try lock: {}", rLock.getName());
            isLocked = rLock.tryLock(0,  3, TimeUnit.SECONDS);

            // 락을 획득하지 못했을 떄
            if (!isLocked) {
                throw new RedisCustomException(RedisEnums.Error.LOCK_NOT_ACQUIRE);
            }

            // 회원 id로 잔액 정보 조회
            Point pointInfo = pointRepository.findByBuyerId(buyerId)
                    .orElseThrow(() -> new PointCustomException(PointEnums.Error.NO_POINT));
            // 잔액 충전
            pointInfo.charge(point);
            // 잔액 충전 내역 저장
            pointHistoryRepository.save(new PointHistory(pointInfo, PointEnums.Type.CHARGE, point));
            return pointInfo;

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

    // 잔액 사용
    @Override
    @Transactional(rollbackFor = {Exception.class})
    public Point usePoint(Long buyerId, int point) {
        // buyerId 기준으로 Lock 객체를 가져옴
        RLock rLock = redissonClient.getLock(RedisEnums.LockName.USE_POINT.changeLockName(buyerId));
        boolean isLocked = false;

        try {
            // 락의 이름으로 RLock 인스턴스 가져옴
            log.info("try lock: {}", rLock.getName());
            isLocked = rLock.tryLock(0,  3, TimeUnit.SECONDS);

            // 락을 획득하지 못했을 떄
            if (!isLocked) {
                throw new RedisCustomException(RedisEnums.Error.LOCK_NOT_ACQUIRE);
            }

            // 회원 id로 잔액 정보 조회
            Point pointInfo = pointRepository.findByBuyerId(buyerId)
                    .orElseThrow(() -> new PointCustomException(PointEnums.Error.NO_POINT));
            // 잔액 사용
            pointInfo.use(point);
            // 잔액 사용 내역 저장
            pointHistoryRepository.save(new PointHistory(pointInfo, PointEnums.Type.DEDUCT, point));
            return pointInfo;

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

    @Override
    public List<PointHistory> findPointHistoryList(Long buyerId) {
        // 회원 id로 잔액 정보 조회
        Point pointInfo = pointRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new PointCustomException(PointEnums.Error.NO_POINT));
        // 잔액 id로 내역 조회
        List<PointHistory> pointHistoryList = pointHistoryRepository.findByPointId(pointInfo.getPointId());
        if(pointHistoryList.isEmpty()){
            return new ArrayList<>();
        }
        return pointHistoryList;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class})
    public void revertPointUsage(Long buyerId, int point) {
        // 사용한 잔액 다시 돌려주기
        Point pointInfo = pointRepository.findByBuyerId(buyerId)
                .orElseThrow(() -> new PointCustomException(PointEnums.Error.NO_POINT));
        pointInfo.charge(point);
    }
}
