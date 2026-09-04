package com.erp.common;

import lombok.Data;

/**
 * 统一返回结构 {code, message, data}
 */
@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public Result() {}
    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> ok() { return new Result<>(200, "成功", null); }
    public static <T> Result<T> ok(T data) { return new Result<>(200, "成功", data); }
    public static <T> Result<T> ok(String msg, T data) { return new Result<>(200, msg, data); }
    public static <T> Result<T> fail(String msg) { return new Result<>(500, msg, null); }
    public static <T> Result<T> fail(int code, String msg) { return new Result<>(code, msg, null); }
}
