package com.xshmq.server.entity;

/**
 * 拉取消息时给服务端的拉取信息
 */
public class CountMessage extends BaseEntity {

    // 所要拉取的队列名称
    private String queueName;
    // 对应队列的消息数量
    private int messageCount;

    public CountMessage() {
    }

    public CountMessage(String queueName, int messageCount) {
        this.queueName = queueName;
        this.messageCount = messageCount;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }
}
