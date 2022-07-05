package com.xshmq.server.entity;

/**
 * 拉取消息时给服务端的拉取信息
 */
public class PullMessage {

    // 所要拉取的队列名称
    private String queueName;
    // 需要拉取消息的数量
    private int messageNum;
    //是否需要顺序拉取消息
    private boolean whetherOrderPull = Boolean.FALSE;

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public int getMessageNum() {
        return messageNum;
    }

    public void setMessageNum(int messageNum) {
        this.messageNum = messageNum;
    }

    public boolean isWhetherOrderPull() {
        return whetherOrderPull;
    }

    public void setWhetherOrderPull(boolean whetherOrderPull) {
        this.whetherOrderPull = whetherOrderPull;
    }
}
