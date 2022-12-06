package com.xshmq.server.entity;

import java.util.List;

public class CountSyncMessage extends AbstractSyncMessage{

    private List<CountMessage> countMessages;

    public CountSyncMessage(List<CountMessage> countMessages) {
        this.countMessages = countMessages;
    }

    public List<CountMessage> getCountMessages() {
        return countMessages;
    }

    public void setCountMessages(List<CountMessage> countMessages) {
        this.countMessages = countMessages;
    }
}
