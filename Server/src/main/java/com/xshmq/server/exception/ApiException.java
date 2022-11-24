package com.xshmq.server.exception;



import com.xshmq.server.enums.StatusCode;

import java.util.Objects;

/**
 * 基础业务异常类
 */
public class ApiException extends RuntimeException{
    private static final long serialVersionUID = -4375114339928877996L;
    private final StatusCode statusCode;

    public ApiException(String message, StatusCode statusCode) {
        super(message);
        this.statusCode = (StatusCode) Objects.requireNonNull(statusCode);
    }

    public ApiException(Throwable cause, StatusCode statusCode) {
        super(cause);
        this.statusCode = (StatusCode)Objects.requireNonNull(statusCode);
    }

    public ApiException(String message, Throwable cause, StatusCode statusCode) {
        super(message, cause);
        this.statusCode = (StatusCode)Objects.requireNonNull(statusCode);
    }

    public boolean isRetryable() {
        return false;
    }

    public StatusCode getStatusCode() {
        return this.statusCode;
    }
}
