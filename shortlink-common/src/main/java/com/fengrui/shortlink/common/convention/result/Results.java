package com.fengrui.shortlink.common.convention.result;
import com.fengrui.shortlink.common.convention.exception.AbstractException;
import com.fengrui.shortlink.common.convention.errorcode.BaseErrorCode;

import java.util.Optional;

/**
 * 全局返回对象工具类
 */
public class Results {
    /**
     * 构造成功相应
     */
    public static Result<Void> success(){
        return new Result<Void>()
                .setCode(BaseErrorCode.SUCCESS.code())
                .setMessage(BaseErrorCode.SUCCESS.message());
    }

    /**
     * 构造带返回数据的成功响应
     */
    public static <T> Result<T> success(T data){
        return new Result<T>()
                .setCode(BaseErrorCode.SUCCESS.code())
                .setMessage(BaseErrorCode.SUCCESS.message())
                .setData((data));
    }

    /**
     * 构建服务端失败相应
     */
    public static Result<Void> failure(){
        return new Result<Void>()
                .setCode(BaseErrorCode.SERVICE_ERROR.code())
                .setMessage(BaseErrorCode.SERVICE_ERROR.message());
    }

    /**
     * 通过 {@link AbstractException} 构建失败响应
     */
    public static Result<Void> failure(AbstractException abstractException){
        String errorCode = Optional.ofNullable(abstractException.getErrorCode())
                .orElse(BaseErrorCode.SERVICE_ERROR.code());
        String errorMessage = Optional.ofNullable(abstractException.getErrorMessage())
                .orElse(BaseErrorCode.SERVICE_ERROR.message());
        return new Result<Void>()
                .setCode(errorCode)
                .setMessage(errorMessage);
    }


    /**
     * 通过 errorCode、errorMessage 构建失败响应
     */
    public static Result<Void> failure(String errorCode, String errorMessage){
        return new Result<Void>()
                .setCode(errorCode)
                .setMessage(errorMessage);
    }


}
