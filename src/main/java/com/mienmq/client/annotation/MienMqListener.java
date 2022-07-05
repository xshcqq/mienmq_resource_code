package com.mienmq.client.annotation;

import com.mienmq.client.config.EnableMienMqRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 跨分区使用的
 * 放在controller方法上标注如何取订单号的注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface MienMqListener {

    /**
     * 队列名
     */
    String queueName() default "";

    /**
     * 客户端是否开启重连
     * @return 默认开启重连
     */
    String topicName() default "";
}
