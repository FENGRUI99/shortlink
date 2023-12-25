package com.fengrui.shortlink.admin.common.convention.exception;

import com.fengrui.shortlink.admin.common.convention.errorcode.BaseErrorCode;
import com.fengrui.shortlink.admin.common.convention.errorcode.IErrorCode;

public class ServiceException extends AbstractException{

    public ServiceException(IErrorCode errorCode){
        this(null, null, errorCode);
    }

    public ServiceException(String message){
        this(message, null, BaseErrorCode.SERVICE_ERROR);
    }

    public ServiceException(String message, IErrorCode errorCode){
        this(message, null, errorCode);
    }

    public ServiceException(String message, Throwable cause, IErrorCode errorCode){
        super(message, cause, errorCode);
    }

    @Override
    public String toString() {
        return "ServiceException{" +
                "errorCode='" + errorCode + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
