package com.locallink.common;

public class FriendException extends BusinessException {
    
    public FriendException(String message) {
        super(ResultCode.FAIL.getCode(), message);
    }
    
    public FriendException(ResultCode resultCode) {
        super(resultCode);
    }
}
