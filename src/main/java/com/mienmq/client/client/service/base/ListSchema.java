package com.mienmq.client.client.service.base;

import java.util.List;


/**
 * 解决ProStuff无法抽象出List然后导致序列化报错问题
 * List序列化抽象类
 */
public class ListSchema {
    private List<PushMessage> pushMessages;

    public ListSchema(){}
    public ListSchema(List<PushMessage> pushMessages) {
        this.pushMessages = pushMessages;
    }

    public List<PushMessage> getPushMessages() {
        return pushMessages;
    }

    public void setPushMessages(List<PushMessage> pushMessages) {
        this.pushMessages = pushMessages;
    }
}
