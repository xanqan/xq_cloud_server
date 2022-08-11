package com.xanqan.project.common;

/**
 * 常用API返回对象接口
 *
 * @author xanqan
 */
public interface IErrorCode {
    /**
     * 状态码
     *
     * @return 状态码
     */
    int getCode();

    /**
     * 状态码信息
     *
     * @return 状态码信息
     */
    String getMessage();

    /**
     *  状态码描述（详情）
     *
     *  @return （详情）
     */
    String getDescription();
}
