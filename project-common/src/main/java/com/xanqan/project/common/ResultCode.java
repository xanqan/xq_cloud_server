package com.xanqan.project.common;

/**
 * 常用API返回对象
 *
 * @author xanqan
 */
public enum ResultCode implements IErrorCode{
    /**
     * 状态码
     */
    SUCCESS(200, "操作成功", ""),
    FAILED(40500, "操作失败", ""),
    PARAMS_ERROR(40401, "请求参数错误", ""),
    NOT_LOGIN(40402, "暂未登录或token已经过期", ""),
    NO_AUTH(40403, "没有相关权限", ""),
    SYSTEM_ERROR(50000, "系统内部异常", "");

    private final int code;

    /**
     * 状态码信息
     */
    private final String message;

    /**
     * 状态码描述（详情）
     */
    private final String description;

    ResultCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
