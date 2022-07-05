package com.mienmq.client.annotation;

import com.mienmq.client.config.EnableMienMqRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 跨分区使用的
 * 放在controller方法上标注如何取订单号的注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
@Import(EnableMienMqRegistrar.class)
public @interface EnableMienMq {

    /**
     * 启动描述
     */
    String mchOrderNoPath() default "开启自定义mq";

    /**
     * 客户端是否开启重连
     * @return 默认开启重连
     */
    boolean whetherReconnect() default true;

    /**
     * 可以手动添加消费者
     * @return
     */
    Class [] clients() default {};
}
