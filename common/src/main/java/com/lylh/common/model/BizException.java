package com.lylh.common.model;

public class BizException extends RuntimeException {

    private final int code;

    public BizException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.code = responseCode.getCode();
    }

    public int getCode() {
        return code;
    }

}
