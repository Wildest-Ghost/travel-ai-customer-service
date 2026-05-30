package com.fintech.common;

import lombok.Getter;

/**
 * 业务异常。
 *
 * 继承 RuntimeException，避免 checked exception 污染方法签名；
 * 带 code 字段，便于 GlobalExceptionHandler 直接转成 Result.error(code, msg)，默认 500。
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 业务错误码 */
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
