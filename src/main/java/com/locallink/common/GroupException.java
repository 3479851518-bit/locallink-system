package com.locallink.common;

public class GroupException extends BusinessException {
    
    public GroupException(String message) {
        super(ResultCode.FAIL.getCode(), message);
    }
    
    public GroupException(ResultCode resultCode) {
        super(resultCode);
    }
}
