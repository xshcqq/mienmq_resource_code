package com.mienmq.client.client;

import com.mienmq.client.client.constants.ConnectionInitConfiguration;
import com.mienmq.client.client.service.impl.NettyInvokeClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;


/**
 * 发送消息客户端FactoryBean
 * @param
 */
public class NettyInvokeClientFactoryBean implements FactoryBean, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(NettyInvokeClientFactoryBean.class);
    private ConnectionInitConfiguration constants;
    private Map<String, String> consumerAnnotationInfo;
    private ApplicationContext applicationContext;

    public void setConsumerAnnotationInfo(Map<String, String> consumerAnnotationInfo) {
        this.consumerAnnotationInfo = consumerAnnotationInfo;
    }
    public void setConstants(ConnectionInitConfiguration constants) {
        this.constants = constants;
    }

    @Override
    public Object getObject() throws Exception {
        return new NettyInvokeClientImpl(constants, consumerAnnotationInfo, applicationContext);
    }

    @Override
    public Class<?> getObjectType() {
        return NettyInvokeClientImpl.class;
    }

    // 是否是单例，默认返回false；
    // 如果不返回true则在项目中用@autoware等注解注入时会再次调用getObject()方法重新生成一个对象
    // 从而导致Spring启动调用一次getObject();其他类自动注入时也会调用一次getObject()方法
    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
