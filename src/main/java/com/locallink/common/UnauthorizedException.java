package com.locallink.common;

public class UnauthorizedException extends BusinessException {
    
    public UnauthorizedException(String message) {
        super(ResultCode.UNAUTHORIZED.getCode(), message);
    }
    
    public UnauthorizedException() {
        super(ResultCode.UNAUTHORIZED);
    }
}
