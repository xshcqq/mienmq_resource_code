package com.mienmq.client.client.producer;


import com.mienmq.client.client.NettyClient;
import com.mienmq.client.client.service.base.BaseEntity;

/**
 * @IntefaceDescrib 消息发送者的抽象接口
 * @IntefaceLink {@link AbstractProducer}的实现类通过{@link NettyClient}来提交IO读写事件；实现客户端之间的消息通信
 */
public interface AbstractProducer<RQ extends BaseEntity> {

    /**
     * mq发送消息
     * @param request
     * @return
     */
    Boolean sendSync (RQ request);

    /**
     * mq异步发送消息
     * @param request
     * @return
     */
    void sendAsync (RQ request);
}
