package com.locallink.common;

public class UserException extends BusinessException {
    
    public UserException(String message) {
        super(ResultCode.FAIL.getCode(), message);
    }
    
    public UserException(ResultCode resultCode) {
        super(resultCode);
    }
}
