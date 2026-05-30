package com.fintech.common;

import lombok.Data;


@Data
public class Result<T> {

    /** 业务状态码：0 成功，其它失败 */
    private Integer code;

    /** 提示消息：成功时是 "success"，失败时是错误描述 */
    private String msg;

    /** 业务数据：泛型，不要写成 Object */
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(0);
        r.setMsg("success");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(int code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}
