package com.lylh.biz.model.common;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CommonResult<T> {

    private int code;
    private String msg;
    private T data;

    public CommonResult() {}

    public CommonResult(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

}
