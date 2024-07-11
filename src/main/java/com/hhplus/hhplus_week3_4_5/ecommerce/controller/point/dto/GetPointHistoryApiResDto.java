package com.hhplus.hhplus_week3_4_5.ecommerce.controller.point.dto;

import com.hhplus.hhplus_week3_4_5.ecommerce.domain.point.PointEnums;
import com.hhplus.hhplus_week3_4_5.ecommerce.domain.point.entity.PointHistory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

public record GetPointHistoryApiResDto(
        @Schema(description = "잔액 내역 ID")
        Long pointHistoryId,
        @Schema(description = "잔액 타입 Enum")
        PointEnums.Type type,
        @Schema(description = "충전/사용한 잔액")
        int usePoint,
        @Schema(description = "잔액 내역 등록일")
        LocalDateTime createDatetime
) implements Serializable {
        public static GetPointHistoryApiResDto from(PointHistory pointHistory){
                return new GetPointHistoryApiResDto(pointHistory.getPointHistoryId(), pointHistory.getType(), pointHistory.getUsePoint(), pointHistory.getCreateDatetime());
        }
}