package com.locallink.common;

public class MessageException extends BusinessException {
    
    public MessageException(String message) {
        super(ResultCode.FAIL.getCode(), message);
    }
    
    public MessageException(ResultCode resultCode) {
        super(resultCode);
    }
}
