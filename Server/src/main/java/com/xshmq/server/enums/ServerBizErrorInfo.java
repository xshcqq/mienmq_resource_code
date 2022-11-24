package com.xshmq.server.enums;


import com.xshmq.server.exception.BizErrorInfo;

public enum ServerBizErrorInfo implements BizErrorInfo {
    INVALID_PARAM("参数错误", StatusCode.INVALID_ARGUMENT),
    ENABLEMIENTMQ_NOT_FIND("启动注解未开启", StatusCode.INVALID_ARGUMENT),
    DATA_TOO_LARGE("传输的数据超过限制", StatusCode.INVALID_ARGUMENT),
    PULL_MESSAGE_ERROR("拉取消息异常", StatusCode.INVALID_ARGUMENT),
    UNKNOWN("系统内部异常", StatusCode.INTERNAL);

    /**
     * message
     */
    String message;

    /**
     * statusCode
     */
    StatusCode statusCode;

    ServerBizErrorInfo(String message, StatusCode statusCode) {
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
