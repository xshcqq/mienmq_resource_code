package com.mienmq.client.client.service.base;

import java.io.Serializable;

/**
 * MQ客户端和服务端交互时返回结果包装类
 */
public class ResultWrapper<R> implements Serializable {

    // 通讯编码
    private boolean netState;

    // 返回结果内容
    private R result;

    // 业务状态
    private boolean busyState;

    public boolean getNetState() {
        return netState;
    }

    public void setNetState(boolean netState) {
        this.netState = netState;
    }

    public R getResult() {
        return result;
    }

    public void setResult(R result) {
        this.result = result;
    }

    public boolean getBusyState() {
        return busyState;
    }

    public void setBusyState(boolean busyState) {
        this.busyState = busyState;
    }
}
