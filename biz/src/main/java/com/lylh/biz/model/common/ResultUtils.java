package com.lylh.biz.model.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lylh.common.model.ResponseCode;

public class ResultUtils {

    public static <T> CommonResult<T> ok(final T data) {
        return CommonResult.<T>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .msg(ResponseCode.SUCCESS.getMessage())
                .data(data)
                .build();
    }

    public static <T> PageListResult<T> ok(final IPage<T> page) {
        PageListResult<T> pageListResult = new PageListResult<>(page);
        pageListResult.setCode(ResponseCode.SUCCESS.getCode());
        pageListResult.setMsg(ResponseCode.SUCCESS.getMessage());
        pageListResult.setData(page.getRecords());
        return pageListResult;
    }

    public static CommonResult<?> ok() {
        return CommonResult.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .msg(ResponseCode.SUCCESS.getMessage())
                .build();
    }

    public static CommonResult<?> error() {
        return CommonResult.builder()
                .code(ResponseCode.FAIL.getCode())
                .msg(ResponseCode.FAIL.getMessage())
                .build();
    }

    public static CommonResult<?> error(final String msg) {
        return CommonResult.builder()
                .code(ResponseCode.FAIL.getCode())
                .msg(msg)
                .build();
    }

    public static CommonResult<?> error(final ResponseCode responseCode) {
        return CommonResult.builder()
                .code(responseCode.getCode())
                .msg(responseCode.getMessage())
                .build();
    }

    public static CommonResult<?> error(final ResponseCode responseCode, final String errorMsg) {
        return CommonResult.builder()
                .code(responseCode.getCode())
                .msg(errorMsg)
                .build();
    }
}
