package com.hhplus.ecommerce.service.point;

import com.hhplus.ecommerce.domain.point.entity.Point;
import com.hhplus.ecommerce.domain.point.entity.PointHistory;

import java.util.List;

public interface PointService {
    // 잔액 조회
    Point findPoint(Long buyerId);

    // 잔액 충전
    Point chargePoint(Long buyerId, int point);
    
    // 잔액 사용
    Point usePoint(Long buyerId, int point);
    
    // 잔액 내역 조회
    List<PointHistory> findPointHistoryList(Long buyerId);

    // 사용한 잔액 다시 돌려주기
    void revertPointUsage(Long buyerId, int point);
}
