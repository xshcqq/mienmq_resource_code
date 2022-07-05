package com.mienmq.client.client.consumer;

import com.mienmq.client.client.exception.BusinessException;
import com.mienmq.client.enums.ClientBizErrorInfo;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class MessagePullListener implements ApplicationContextAware, ApplicationListener<MessagePullEvent> {

    private ApplicationContext context;

    /**
     * 当拉取消息发现消费队列之前是空的就会由阻塞状态转为消费状态
     * {@link MessagePullEvent}
     * @param event
     */
    @Override
    public void onApplicationEvent(MessagePullEvent event) {
        AtomicReference<String> source = (AtomicReference<String>)event.getSource(); // (beanName )
        DefaultConsumer consumer = (DefaultConsumer) context.getBean(source.get());
        if (consumer == null) {
            throw new BusinessException(ClientBizErrorInfo.UNKNOWN, "找不到对应消费者实例！");
        }
        consumer.changeQueueStatusToReady();
        consumer.invokeListener();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
