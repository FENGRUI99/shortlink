package com.fengrui.shortlink.admin.common.convention.exception;

import com.fengrui.shortlink.admin.common.convention.errorcode.IErrorCode;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 抽象项目中三类异常体系，客户端异常、服务端异常以及远程服务调用异常
 * @see ClientException
 * @see ServiceException
 * @see RemoteException
 */
@Getter
public class AbstractException extends RuntimeException{
    public final String errorCode;

    public final String errorMessage;

    /**
     * 优先errorMessage，其次errorCode.message()
     */
    public AbstractException(String errorMessage, Throwable cause, IErrorCode errorCode){
        super(errorMessage, cause);
        this.errorCode = errorCode.code();
        System.out.println(errorCode.message());
        this.errorMessage = Optional.ofNullable(StringUtils.hasLength(errorMessage) ? errorMessage : null).orElse(errorCode.message());
    }
}
