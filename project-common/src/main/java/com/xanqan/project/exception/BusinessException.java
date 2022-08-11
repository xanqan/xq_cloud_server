package com.xanqan.project.exception;

import com.xanqan.project.common.ResultCode;
import lombok.Getter;

/**
 * 自定义封装异常
 *
 * @author xanqan
 */
@Getter
public class BusinessException extends RuntimeException{

    private final int code;

    private final String description;

    public BusinessException(String message, int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(ResultCode code) {
        super(code.getMessage());
        this.code = code.getCode();
        this.description = code.getDescription();
    }

    public BusinessException(ResultCode code, String description) {
        super(code.getMessage());
        this.code = code.getCode();
        this.description = description;
    }
}
