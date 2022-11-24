package com.xshmq.server.exception;


import com.xshmq.server.enums.StatusCode;

import java.util.Objects;

/**
 * 业务异常类
 */
public class ServerException extends ApiException{
    private static final long serialVersionUID = 457501262311893386L;
    private final BizErrorInfo bizErrorInfo;

    public ServerException(BizErrorInfo bizErrorInfo) {
        super(((BizErrorInfo) Objects.requireNonNull(bizErrorInfo)).getMessage(), bizErrorInfo.getStatus());
        this.bizErrorInfo = bizErrorInfo;
    }

    public ServerException(BizErrorInfo bizErrorInfo, String message) {
        super(message, ((BizErrorInfo)Objects.requireNonNull(bizErrorInfo)).getStatus());
        this.bizErrorInfo = bizErrorInfo;
    }

    public ServerException(BizErrorInfo bizErrorInfo, Throwable throwable) {
        super(((BizErrorInfo)Objects.requireNonNull(bizErrorInfo)).getMessage(), throwable, bizErrorInfo.getStatus());
        this.bizErrorInfo = bizErrorInfo;
    }

    public ServerException(BizErrorInfo bizErrorInfo, String message, Throwable throwable) {
        super(message, throwable, ((BizErrorInfo)Objects.requireNonNull(bizErrorInfo)).getStatus());
        this.bizErrorInfo = bizErrorInfo;
    }

    public BizErrorInfo getBizErrorInfo() {
        return this.bizErrorInfo;
    }

    public static class SimpleBizErrorInfo implements BizErrorInfo {
        private final String bizCode;
        private final String message;
        private final StatusCode status;

        public SimpleBizErrorInfo(String bizCode, String message, StatusCode status) {
            this.bizCode = bizCode;
            this.message = message;
            this.status = status;
        }

        public String getBizCode() {
            return this.bizCode;
        }

        public String getMessage() {
            return this.message;
        }

        public StatusCode getStatus() {
            return this.status;
        }
    }
}
