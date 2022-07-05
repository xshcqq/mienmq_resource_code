package com.mienmq.client.client.constants;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 客户端获取连接配置路径
 */
public class ConnectionConstants {

    public final static String host =  "mienmq.producer.host";
    public final static String port =  "mienmq.producer.port";
    public final static String whetherReconnect = "mienmq.producer.whetherReconnect";
    public final static String retryTimes =  "mienmq.producer.retryTimes";
    public final static String pullMsgNum =  "mienmq.producer.pullMsgNum";
    public final static String consumeFailMaxTimes =  "mienmq.producer.consumeFailMaxTimes";
}
