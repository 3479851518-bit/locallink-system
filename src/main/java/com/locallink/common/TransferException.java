package com.locallink.common;

public class TransferException extends BusinessException {
    
    public TransferException(String message) {
        super(ResultCode.FAIL.getCode(), message);
    }
    
    public TransferException(ResultCode resultCode) {
        super(resultCode);
    }
}
