package com.mienmq.client.client.service.impl;

import com.alibaba.fastjson.JSON;
import com.mienmq.client.client.exception.BusinessException;
import com.mienmq.client.client.producer.DefaultProducer;
import com.mienmq.client.client.service.SendMessageService;
import com.mienmq.client.client.service.base.*;
import com.mienmq.client.enums.ClientBizErrorInfo;
import com.mienmq.client.enums.RequestType;
import com.mienmq.client.util.ProtostuffUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class SendMessageServiceImpl implements SendMessageService {

    private final static Logger logger = LoggerFactory.getLogger(SendMessageServiceImpl.class);

    @Autowired
    private NettyInvokeClientImpl client;
    @Autowired
    private DefaultProducer producer;

    /**
     * mq发送消息
     * @param request
     * @return
     */
    @Override
    public ResultWrapper send(PushMessage request) {
        if (request == null){
            throw new BusinessException(ClientBizErrorInfo.INVALID_PARAM, "发送消息体类型不正确，必须集成BaseEntity!");
        }
        Message message = new Message();
        message.setMessageId("" + System.currentTimeMillis() + UUID.randomUUID());
        message.setRequestType(RequestType.SEND_MESSAGE.name());
        message.setQueueName("DEFAULT_QUEUE");
        request.setQueueName("DEFAULT_QUEUE");
        message.setContent(ProtostuffUtil.serializer(request));

        logger.info("开始发送同步消息：{}", JSON.toJSONString(request));
        ResultWrapper wrapper = client.sendSync(message);
        logger.info("结束发送同步消息，返回结果为：{}", JSON.toJSONString(wrapper));
        return wrapper;
    }


    /**
     * mq发送异步消息
     * @param request
     * @return
     */
    @Override
    public ResultWrapper sendOneWay(PushMessage request) {
        if (request == null){
            throw new BusinessException(ClientBizErrorInfo.INVALID_PARAM, "发送消息体类型不正确，必须集成BaseEntity!");
        }
        Message message = new Message();
        message.setMessageId("" + System.currentTimeMillis() + UUID.randomUUID());
        message.setRequestType(RequestType.SEND_MESSAGE.name());
        message.setQueueName("DEFAULT_QUEUE");
        request.setQueueName("DEFAULT_QUEUE");
        message.setContent(ProtostuffUtil.serializer(request));

        logger.info("开始发送异步消息：{}", JSON.toJSONString(request));
        ResultWrapper wrapper = client.sendAsync(message);
        logger.info("结束发送异步消息，返回结果为：{}", JSON.toJSONString(wrapper));
        return wrapper;
    }



    /**
     * mq发送拉取消息请求
     * @param request
     * @return
     */
    @Override
    public ResultWrapper send(PullMessage request) {
        if (request == null){
            throw new BusinessException(ClientBizErrorInfo.INVALID_PARAM, "发送消息体类型不正确，必须集成BaseEntity!");
        }
        Message message = new Message();
        message.setMessageId("" + System.currentTimeMillis() + UUID.randomUUID());
        message.setRequestType(RequestType.SEND_MESSAGE.name());
        message.setQueueName("DEFAULT_QUEUE");
        request.setQueueName("DEFAULT_QUEUE");
        message.setContent(ProtostuffUtil.serializer(request));

        logger.info("开始发送同步消息：{}", JSON.toJSONString(request));
        ResultWrapper wrapper = client.sendSync(message);
        logger.info("结束发送同步消息，返回结果为：{}", JSON.toJSONString(wrapper));
        return wrapper;
    }


    /**
     * mq发送消息
     * @param request
     * @return
     */
    @Override
    public ResultWrapper sendListener_2(PushMessage request) {
        if (request == null){
            throw new BusinessException(ClientBizErrorInfo.INVALID_PARAM, "发送消息体类型不正确，必须集成BaseEntity!");
        }
        Message message = new Message();
        message.setMessageId("" + System.currentTimeMillis() + UUID.randomUUID());
        message.setRequestType(RequestType.SEND_MESSAGE.name());
        message.setQueueName("DEFAULT_QUEUE_2");
        request.setQueueName("DEFAULT_QUEUE_2");
        message.setContent(ProtostuffUtil.serializer(request));

        logger.info("开始发送同步消息：{}", JSON.toJSONString(request));
        ResultWrapper wrapper = producer.sendSync(message);
        logger.info("结束发送同步消息，返回结果为：{}", JSON.toJSONString(wrapper));
        return wrapper;
    }
}
