package com.mienmq.client.client.service.base;


public class MessageRequestBody extends BaseEntity {

    private String content;

    private String messageId;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
