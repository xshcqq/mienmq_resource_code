package com.mienmq.client.config;

import com.mienmq.client.client.NettyInvokeClientFactoryBean;
import com.mienmq.client.client.constants.ConnectionInitConfiguration;
import com.mienmq.client.client.producer.DefaultProducer;
import com.mienmq.client.client.service.SendMessageService;
import com.mienmq.client.client.service.impl.SendMessageServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(MessageListenerBeanPostProcessor.class)
@EnableConfigurationProperties(ConnectionInitConfiguration.class)
public class MienMqAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SendMessageService makeSendMessageService () {
        return new SendMessageServiceImpl();
    }

    @Bean
    @ConditionalOnBean(NettyInvokeClientFactoryBean.class)
    @ConditionalOnMissingBean
    public DefaultProducer makeDefaultProducer () {
        return new DefaultProducer();
    }

//    @Bean
//    @ConditionalOnBean(NettyInvokeClientFactoryBean.class)
//    @ConditionalOnMissingBean
//    public DefaultConsumer makeDefaultConsumer (NettyInvokeClient nettyInvokeClientImpl) {
//        return new DefaultConsumer(nettyInvokeClientImpl);
//    }
}
