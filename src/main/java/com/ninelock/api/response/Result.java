package com.ninelock.api.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

import static java.net.HttpURLConnection.*;

/**
 * @author gongym
 * @version 创建时间: 2023-12-13 16:58
 */
@Data
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 成功标识
     */
    private boolean success;

    /**
     * 消息
     */
    private String message;

    /**
     * 代码
     */
    private Integer code;

    /**
     * 数据对象 data
     */
    private T data;

    /**
     * 当前时间戳
     */
    private long timestamp = System.currentTimeMillis();

    public static <T> Result<T> ok() {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setCode(HTTP_OK);
        return result;
    }

    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setCode(HTTP_OK);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> ok(String msg, T data) {
        Result<T> result = new Result<>();
        result.setSuccess(true);
        result.setCode(HTTP_OK);
        result.setMessage(msg);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> error(String msg, T data) {
        Result<T> result = new Result<>();
        result.setSuccess(false);
        result.setCode(HTTP_INTERNAL_ERROR);
        result.setMessage(msg);
        result.setData(data);
        return result;
    }

    public static Result<Object> error(int code, String msg) {
        Result<Object> result = new Result<>();
        result.setCode(code);
        result.setMessage(msg);
        result.setSuccess(false);
        return result;
    }

    public static <T> Result<T> error400(String message) {
        Result<T> result = new Result<>();
        result.setCode(HTTP_BAD_REQUEST);
        result.setMessage(message);
        result.setSuccess(false);
        return result;
    }

    public static <T> Result<T> error401(String message) {
        Result<T> result = new Result<>();
        result.setCode(HTTP_UNAUTHORIZED);
        result.setMessage(message);
        result.setSuccess(false);
        return result;
    }

    public static <T> Result<T> error403(String message) {
        Result<T> result = new Result<>();
        result.setCode(HTTP_FORBIDDEN);
        result.setMessage(message);
        result.setSuccess(false);
        return result;
    }

    public static <T> Result<T> error404(String message) {
        Result<T> result = new Result<>();
        result.setCode(HTTP_NOT_FOUND);
        result.setMessage(message);
        result.setSuccess(false);
        return result;
    }

    public static <T> Result<T> error405(String message) {
        Result<T> result = new Result<>();
        result.setCode(HTTP_BAD_METHOD);
        result.setMessage(message);
        result.setSuccess(false);
        return result;
    }

    public static <T> Result<T> error500(String message) {
        Result<T> result = new Result<>();
        result.setCode(HTTP_INTERNAL_ERROR);
        result.setMessage(message);
        result.setSuccess(false);
        return result;
    }
}