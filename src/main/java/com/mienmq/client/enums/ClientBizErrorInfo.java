package com.mienmq.client.enums;

import com.mienmq.client.client.exception.BizErrorInfo;

public enum ClientBizErrorInfo implements BizErrorInfo {
    INVALID_PARAM("参数错误", StatusCode.INVALID_ARGUMENT),
    ENABLEMIENTMQ_NOT_FIND("启动注解未开启", StatusCode.INVALID_ARGUMENT),
    DATA_TOO_LARGE("传输的数据超过限制", StatusCode.INVALID_ARGUMENT),
    UNKNOWN("系统内部异常", StatusCode.INTERNAL);

    /**
     * message
     */
    String message;

    /**
     * statusCode
     */
    StatusCode statusCode;

    ClientBizErrorInfo(String message, StatusCode statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }

    @Override
    public String getBizCode() {
        return "AGS_" + this.name();
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public StatusCode getStatus() {
        return statusCode;
    }
}
