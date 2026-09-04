package com.j180.erp.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结构 { code, msg, data }
 */
@Data
public class Result<T> implements Serializable {

    private int code;
    private String msg;
    private T data;

    public static final int SUCCESS = 200;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int SERVER_ERROR = 500;

    private Result() {
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.code = SUCCESS;
        result.msg = "success";
        result.data = data;
        return result;
    }

    public static <T> Result<T> fail(int code, String msg) {
        Result<T> result = new Result<>();
        result.code = code;
        result.msg = msg;
        return result;
    }
}
