package com.j180.erp.common;

import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        this(Result.BAD_REQUEST, message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }
}
