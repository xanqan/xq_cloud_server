package com.xanqan.project.common;

import lombok.Data;

/**
 * 通用返回对象
 *
 * @author xanqan
 */
@Data
public class BaseResponse<T> {
    /**
     * 状态码
     */
    private int code;
    /**
     * 状态码信息
     */
    private String message;
    /**
     * 状态码描述（详情）
     */
    private String description;
    /**
     * 返回数据
     */
    private T data;

    public BaseResponse(int code, String message, String description, T data) {
        this.code = code;
        this.message = message;
        this.description = description;
        this.data = data;
    }

    public BaseResponse(int code, String message, T data) {
        this(code, message, null, data);
    }

    public BaseResponse(int code, String message, String description) {
        this(code, message, description, null);
    }

    public BaseResponse(ResultCode code) {
        this(code.getCode(), code.getMessage(), code.getDescription(), null);
    }

    public BaseResponse(ResultCode code, String description) {
        this(code.getCode(), code.getMessage(), description, null);
    }
}
