package com.lylh.common.model;

/**
 * 业务失败。调用方用错误码处理，不把异常当成分支跳转以外的控制流。
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.code = responseCode.getCode();
    }

    /**
     * 保留原始异常，方便日志里看到真正的失败原因，而不是只剩一句业务文案。
     *
     * @param responseCode 对外错误码
     * @param cause        原始异常
     */
    public BizException(ResponseCode responseCode, Throwable cause) {
        super(responseCode.getMessage(), cause);
        this.code = responseCode.getCode();
    }

    public int getCode() {
        return code;
    }

}
