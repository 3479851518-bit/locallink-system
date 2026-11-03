package com.locallink.common;

public class ParamException extends BusinessException {
    
    public ParamException(String message) {
        super(ResultCode.PARAM_ERROR.getCode(), message);
    }
    
    public ParamException() {
        super(ResultCode.PARAM_ERROR);
    }
}
