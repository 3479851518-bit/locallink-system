package com.locallink.common;

public class FileException extends BusinessException {
    
    public FileException(String message) {
        super(ResultCode.FAIL.getCode(), message);
    }
    
    public FileException(ResultCode resultCode) {
        super(resultCode);
    }
}
