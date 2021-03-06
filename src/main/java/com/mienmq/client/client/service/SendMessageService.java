package com.mienmq.client.client.service;


import com.mienmq.client.client.service.base.*;

/**
 * mq测试发送消息服务
 */
public interface SendMessageService<RP extends ResultWrapper, PS extends PushMessage, PL extends PullMessage> {

    /**
     * mq发送消息
     * @param request
     * @return
     */
    RP send (PS request);

    /**
     * mq发送异步消息
     * @param request
     * @return
     */
    RP sendOneWay (PS request);

    /**
     * mq发送消息
     * @param request
     * @return
     */
    RP send (PL request);

    /**
     * mq发送消息
     * @param request
     * @return
     */
    RP sendListener_2 (PS request);

}
