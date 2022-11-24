package com.xshmq.server.exception;


import com.xshmq.server.enums.StatusCode;

public interface BizErrorInfo {

    String getBizCode();

    String getMessage();

    StatusCode getStatus();
}
