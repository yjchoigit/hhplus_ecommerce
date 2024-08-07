package com.hhplus.ecommerce.base.exception.reponse.dto;

import com.hhplus.ecommerce.base.exception.reponse.BaseEnums;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseDto<T> {
    private final BaseEnums.ResponseStatus status;
    private final Error error;
    private final T data;

    @Getter
    @AllArgsConstructor
    public static class Error{
        private String errorCode;
        private String message;
    }
}