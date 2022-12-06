package com.mienmq.client.client.service.base;


public class CountSyncReqMessage extends AbstractSyncMessage{
    private String queueName;

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
}
