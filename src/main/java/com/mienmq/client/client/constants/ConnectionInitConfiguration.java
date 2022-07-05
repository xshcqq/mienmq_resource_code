package com.mienmq.client.client.constants;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 消息生产者配置实体类
 */
@ConfigurationProperties(prefix = "mienmq.producer")
public class ConnectionInitConfiguration {

    private String host;

    private String port;

    private boolean whetherReconnect;

    private int retryTimes;
    //拉取任务每次拉取的消息数
    private int pullMsgNum;
    //消费消息最大失败次数
    private int consumeFailMaxTimes;

    public ConnectionInitConfiguration(){}
    //Full Constructor
    public ConnectionInitConfiguration(String host, String port, boolean whetherReconnect, int retryTimes,
                                       int pullMsgNum, int consumeFailMaxTimes) {
        this.host = host;
        this.port = port;
        this.whetherReconnect = whetherReconnect;
        this.retryTimes = retryTimes;
        this.pullMsgNum = pullMsgNum;
        this.consumeFailMaxTimes = consumeFailMaxTimes;
    }

    public int getPullMsgNum() {
        return pullMsgNum;
    }

    public void setPullMsgNum(int pullMsgNum) {
        this.pullMsgNum = pullMsgNum;
    }

    public int getConsumeFailMaxTimes() {
        return consumeFailMaxTimes;
    }

    public void setConsumeFailMaxTimes(int consumeFailMaxTimes) {
        this.consumeFailMaxTimes = consumeFailMaxTimes;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public boolean getWhetherReconnect() {
        return whetherReconnect;
    }

    public void setWhetherReconnect(boolean whetherReconnect) {
        this.whetherReconnect = whetherReconnect;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
