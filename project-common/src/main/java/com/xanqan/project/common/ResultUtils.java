package com.xanqan.project.common;

/**
 * 返回工具类
 *
 * @author xanqan
 */
public class ResultUtils {

    /**
     * 成功
     *
     * @param data 返回数据
     * @return 通用返回对象
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 失败
     *
     * @param code 状态码返回
     * @return 通用返回对象
     */
    public static <T> BaseResponse<T> error(ResultCode code) {
        return new BaseResponse<>(code);
    }

    /**
     * 失败
     *
     * @param code 状态码返回
     * @param description 状态码描述（详情）返回
     * @return 通用返回对象
     */
    public static <T> BaseResponse<T> error(ResultCode code,String description) {
        return new BaseResponse<>(code, description);
    }

    /**
     * 失败
     *
     * @param code 状态码返回
     * @param massage 状态码信息返回
     * @param description 状态码描述（详情）返回
     * @return 通用返回对象
     */
    public static <T> BaseResponse<T> error(int code, String massage,String description) {
        return new BaseResponse<>(code, massage, description);
    }

}
