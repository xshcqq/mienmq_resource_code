package com.mienmq.client.client.consumer;

import com.mienmq.client.client.constants.ConnectionInitConfiguration;
import com.mienmq.client.client.service.NettyInvokeClient;
import com.mienmq.client.client.service.base.Message;
import com.mienmq.client.client.service.base.PullMessage;
import com.mienmq.client.enums.RequestType;
import com.mienmq.client.util.ProtostuffUtil;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 默认消费者
 */
public class DefaultConsumer {
    // 定时从服务端拉取消息
    private static ScheduledThreadPoolExecutor pullMessagesExecutor;
    // netty客户端
    private NettyInvokeClient nettyInvokeClient;
    // 客户端yml中的配置
    private ConnectionInitConfiguration constants;
    private final Message pullRequestCache = new Message();
    private ThreadPoolExecutor defaultConsumeExecutor;
    private String queueName; //监听的队列名
    private String topicName; //监听的队列名
    private volatile AtomicBoolean whetherEmpty = new AtomicBoolean(true);

    public DefaultConsumer() {
        this.pullMessagesExecutor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "pullMessagesExecutor_" + this.threadIndex.incrementAndGet());
            }
        });
        // 初始化消费消息线程池
        this.defaultConsumeExecutor = new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors()/2,
                Runtime.getRuntime().availableProcessors(),
                1000 * 30,
                TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(),
                new ThreadFactory() {
                    private AtomicInteger threadIndex = new AtomicInteger(0);
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "AsyncSenderExecutor_" + this.threadIndex.incrementAndGet());
                    }
                });
    }

    public void setProAndInitConsumer(NettyInvokeClient nettyInvokeClient) {
        this.nettyInvokeClient = nettyInvokeClient;
        this.queueName = nettyInvokeClient.getQueueName(this.getClass().getCanonicalName());
        this.constants = nettyInvokeClient.getClientConfiguration();
        // 初始化并缓存定时任务每次拉取消息的请求内容
        initPullRequestCache(pullRequestCache);
        // 开启拉取消息定时任务
        startPullWorkersIfReady();
    }

    /**
     * 发起拉取消息请求 默认一秒钟拉取一次
     * @return
     */
    private void startPullWorkersIfReady() {
        // 如果启动时；本地持久化未消费消息文件不是空的；则先将里面的消息搬到本地消息阻塞队列中
        if (false) {
        }
        pullMessagesExecutor.scheduleAtFixedRate(() -> {
            nettyInvokeClient.sendAsync(pullRequestCache);
        }, 0L,1L, TimeUnit.SECONDS);
    }

    /**
     * 初始化并缓存定时任务每次拉取消息的请求内容
     * @param pullRequestCache
     */
    private void initPullRequestCache(final Message pullRequestCache) {
        PullMessage pullRequest = new PullMessage();
        pullRequest.setMessageNum(constants.getPullMsgNum());
        pullRequest.setMessageNum(constants.getPullMsgNum());
        pullRequest.setWhetherOrderPull(constants.getWhetherReconnect());
        pullRequest.setQueueName(queueName);

        pullRequestCache.setMessageId("" + System.currentTimeMillis() + UUID.randomUUID());
        pullRequestCache.setRequestType(RequestType.PULL_MESSAGE.name());
        pullRequestCache.setQueueName(queueName);
        pullRequestCache.setContent(ProtostuffUtil.serializer(pullRequest));
    }

    /**
     * 启动消费
     */
    public void invokeListener() {
        while (!whetherEmpty.get()) {
            defaultConsumeExecutor.execute(() -> {
                Object singleMessage = nettyInvokeClient.getSingleMessage(queueName);
                if (singleMessage == null) {
                    // 将队列状态置为空（true）
                    whetherEmpty.compareAndSet(Boolean.FALSE, Boolean.TRUE);
                    return;
                } else {
                    onMessage(singleMessage);
                }
            });
        }
    }

    /**
     * 将队列状态置为就绪状态
     */
    public void changeQueueStatusToReady() {
        // 将队列状态置为有消息状态
        whetherEmpty.compareAndSet(Boolean.TRUE, Boolean.FALSE);
    }


    /**
     * 子类消费逻辑
     */
    public void onMessage(Object message) {
    }

}
