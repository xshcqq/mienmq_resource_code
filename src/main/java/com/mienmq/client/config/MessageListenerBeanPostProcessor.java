package com.mienmq.client.config;

import com.mienmq.client.client.consumer.DefaultConsumer;
import com.mienmq.client.client.producer.DefaultProducer;
import com.mienmq.client.client.service.NettyInvokeClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;


public class MessageListenerBeanPostProcessor implements SmartInitializingSingleton, ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void afterSingletonsInstantiated() {
        Map<String, DefaultConsumer> consumerMap = applicationContext.getBeansOfType(DefaultConsumer.class);
        Map<String, DefaultProducer> producerMap = applicationContext.getBeansOfType(DefaultProducer.class);
        NettyInvokeClient client = applicationContext.getBean(NettyInvokeClient.class);
         // 设置消费者netty客户端属性
        consumerMap.values().forEach((defaultConsumer) -> {
            defaultConsumer.setProAndInitConsumer(client);
            defaultConsumer.invokeListener();
        });

        // 设置生产者netty客户端属性
        producerMap.values().forEach((producer) -> {
            producer.setProAndInitProducer(client);
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
