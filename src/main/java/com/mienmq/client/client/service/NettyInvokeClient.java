package com.mienmq.client.client.service;


import com.mienmq.client.client.constants.ConnectionInitConfiguration;
import com.mienmq.client.client.service.base.BaseEntity;
import com.mienmq.client.client.service.base.Message;
import com.mienmq.client.client.service.base.PullMessage;
import com.mienmq.client.client.service.base.ResultWrapper;

import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * 通过{@link com.mienmq.client.client.NettyClient}获取连接；执行同步或者异步发送消息
 * 发送消息服务
 */
public interface NettyInvokeClient<M> {

    /**
     * 同步发送消息
     * @param requestMsg
     * @return {@link ResultWrapper}
     */
    ResultWrapper sendSync (M requestMsg);

    /**
     * 异步发送消息
     * @param requestMsg
     * @return {@link ResultWrapper}
     */
    ResultWrapper sendAsync (M requestMsg);

    /**
     * 获取客户端配置类
     * @return {@link ConnectionInitConfiguration}
     */
    ConnectionInitConfiguration getClientConfiguration();

    /**
     * 根据队列名获取消费线程池对应的消费队列
     * @param queueName
     * @return {@link LinkedBlockingQueue}
     */
    LinkedBlockingQueue getQueueByQueueName(String queueName);

    /**
     * 获取单条消息
     * @param queueName
     * @return {@link Object} #PullMessage
     */
    Object getSingleMessage(String queueName);

    /**
     * 获取消费者对应队列名
     * @param beanName
     * @return {@link String}
     */
    String getQueueName(String beanName);
}
