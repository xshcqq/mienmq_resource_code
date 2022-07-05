package com.mienmq.client.client.constants;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 客户端配置常量类
 */
public class ConsumerConstants {

    public final static int defaultPullMessageNum = 16;
    public final static int minPullMessageNum = 1;
    public final static int maxPullMessageNum = 32;
    public final static int minRetryTimes = 32;
    public final static int maxConsumeFailMaxTimes = 16;
    public final static String QUEUE_NAME = "queueName";
    public final static String TOPIC_NAME = "topicName";
}
