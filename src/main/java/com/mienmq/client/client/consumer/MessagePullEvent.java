package com.mienmq.client.client.consumer;

import org.springframework.context.ApplicationEvent;

public class MessagePullEvent extends ApplicationEvent {

//    private String queueName; // 队列名
//
//    public String getQueueName() {
//        return queueName;
//    }
//
//    public void setQueueName(String queueName) {
//        this.queueName = queueName;
//    }

    public MessagePullEvent(Object source) {
        super(source);
    }
}
