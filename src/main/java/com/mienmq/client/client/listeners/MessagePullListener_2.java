package com.mienmq.client.client.listeners;

import com.alibaba.fastjson.JSON;
import com.mienmq.client.annotation.MienMqListener;
import com.mienmq.client.client.consumer.DefaultConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MienMqListener(queueName = "NOTICE_TEST")
public class MessagePullListener_2 extends DefaultConsumer {
    private final static Logger logger = LoggerFactory.getLogger(MessagePullListener_2.class);


    @Override
    public void onMessage(Object message) {
        logger.info("消费者【{}】, 接收到消息：{}", this.getClass().getName(), JSON.toJSONString(message));
    }
}
