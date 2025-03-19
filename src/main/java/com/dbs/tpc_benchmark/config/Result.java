package com.dbs.tpc_benchmark.config;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private Integer code; //编码：200成功，0和其它数字为失败
    private String message; //错误信息
    private T data; //数据

    public static <T> Result<T> success(String message) {
        Result<T> result = new Result<T>();
        result.code = 200;
        result.message = message;
        return result;
    }

    public static <T> Result<T> success(T object,String message) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = 200;
        result.message = message;
        return result;
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<T>();
        result.code = 400;
        result.message = message;
        return result;
    }

    public static <T> Result<T> forbidden(String message) {
        Result<T> result = new Result<T>();
        result.code = 403;
        result.message = message;
        return result;
    }

    public static <T> Result<T> error(Integer code,T object,String message) {
        Result<T> result = new Result<>();
        result.message = message;
        result.code = code;
        result.data = object;
        return result;
    }

}