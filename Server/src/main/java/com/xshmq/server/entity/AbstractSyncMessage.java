package com.xshmq.server.entity;

public class AbstractSyncMessage extends BaseEntity {

    /**
     * 客户端发送同步消息是会上送对应的线程id
     */
    private String threadId;

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }
}
