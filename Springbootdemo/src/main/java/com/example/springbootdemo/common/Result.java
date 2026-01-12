package com.example.springbootdemo.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 返回统一结果类
 */
@Data
public class Result<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // 状态码
    private int code;

    // 提示信息
    private String msg;

    // 返回的数据
    private T data;

    /**
     * 直接返回成功结果（带数据）
     * @param data 返回的数据
     * @return Result对象
     */
    public static <T> Result<T> success(T data) {
        return success(200, "操作成功", data);
    }

    /**
     * 自定义返回成功结果
     * @param code 状态码
     * @param msg 提示信息
     * @param data 返回的数据
     * @return Result对象
     */
    public static <T> Result<T> success(int code, String msg, T data) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }

    /**
     * 不带数据返回成功
     * @return Result对象
     */
    public static <T> Result<T> success() {
        return success(200, "操作成功", null);
    }

    /**
     * 直接返回失败信息
     * @return Result对象
     */
    public static <T> Result<T> error() {
        return error(400, "操作失败", null);
    }

    /**
     * 带参数返回失败信息
     * @param msg 提示信息
     * @return Result对象
     */
    public static <T> Result<T> error(String msg) {
        return error(400, msg, null);
    }

    /**
     * 自定义返回失败信息
     * @param code 状态码
     * @param msg 提示信息
     * @param data 返回的数据
     * @return Result对象
     */
    public static <T> Result<T> error(int code, String msg, T data) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }
}