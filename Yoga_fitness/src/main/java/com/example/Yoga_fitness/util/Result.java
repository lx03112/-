package com.example.Yoga_fitness.util;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;       // 状态码（200=成功，404=失败等）
    private String msg;         // 提示信息
    private T data;             // 返回数据（如登录用户信息）
    private long timestamp;     // 请求时间戳（便于排查问题）

    // 成功响应：返回数据+默认成功信息
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    // 成功响应：自定义信息+数据
    public static <T> Result<T> success(String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg(msg);
        result.setData(data);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    // 失败响应：自定义错误信息+状态码
    public static <T> Result<T> fail(String msg, Integer code) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(null);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    // 失败响应：自定义错误信息（使用默认状态码500）
    public static <T> Result<T> fail(String msg) {
        return fail(msg, 500);
    }
}