package com.locallink.common;

public class AdminException extends BusinessException {
    
    public AdminException(String message) {
        super(ResultCode.FAIL.getCode(), message);
    }
    
    public AdminException(ResultCode resultCode) {
        super(resultCode);
    }
}
