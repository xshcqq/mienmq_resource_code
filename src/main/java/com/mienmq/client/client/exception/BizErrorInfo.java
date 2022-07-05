package com.mienmq.client.client.exception;

import com.mienmq.client.enums.StatusCode;

public interface BizErrorInfo {

    String getBizCode();

    String getMessage();

    StatusCode getStatus();
}
