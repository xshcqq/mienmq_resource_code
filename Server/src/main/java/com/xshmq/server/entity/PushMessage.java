package com.xshmq.server.entity;

/**
 * 拉取消息时给服务端的拉取信息
 */
public class PushMessage {

    // 所要拉取的队列名称
    private String queueName;

    // 内容
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

}
