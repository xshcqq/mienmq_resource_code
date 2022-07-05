package com.mienmq.client.client.producer;

import com.mienmq.client.client.service.NettyInvokeClient;
import com.mienmq.client.client.service.base.Message;
import com.mienmq.client.client.service.base.ResultWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultProducer {
    private final static Logger logger = LoggerFactory.getLogger(DefaultProducer.class);

    private NettyInvokeClient sendMessageClient;

    public void setProAndInitProducer (NettyInvokeClient sendMessageClient) {
        this.sendMessageClient = sendMessageClient;
    }

    /**
     * 异步发送消息
     * @param message
     * @return
     */
    public ResultWrapper sendAsync(Message message) {
        return sendMessageClient.sendAsync(message);
    }

    /**
     * 同步发送消息
     * @param message
     * @return
     */
    public ResultWrapper sendSync(Message message) {
        return sendMessageClient.sendSync(message);
    }
}
