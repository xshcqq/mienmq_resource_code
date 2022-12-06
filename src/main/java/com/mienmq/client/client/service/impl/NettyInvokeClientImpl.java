package com.mienmq.client.client.service.impl;

import com.alibaba.fastjson.JSON;
import com.mienmq.client.client.NettyClient;
import com.mienmq.client.client.constants.ConnectionInitConfiguration;
import com.mienmq.client.client.consumer.MessagePullEvent;
import com.mienmq.client.client.exception.BusinessException;
import com.mienmq.client.client.handler.LifeCycleInBoundHandler;
import com.mienmq.client.client.handler.MsgPackDecoder;
import com.mienmq.client.client.handler.MsgPackEncoder;
import com.mienmq.client.client.handler.ProtoStuffEncodeClientHandler;
import com.mienmq.client.client.service.NettyInvokeClient;
import com.mienmq.client.client.service.base.Message;
import com.mienmq.client.client.service.base.PullMessage;
import com.mienmq.client.client.service.base.PushMessage;
import com.mienmq.client.client.service.base.ResultWrapper;
import com.mienmq.client.enums.ClientBizErrorInfo;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.internal.StringUtil;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * netty连接；发送消息接口{@link NettyInvokeClient}实现类
 */
public class NettyInvokeClientImpl implements NettyInvokeClient {
    private final static Logger logger = LoggerFactory.getLogger(NettyClient.class);
    private static final Lock queueNameMapLock = new ReentrantLock();
    private final static Map<String, Channel> channelCache = new ConcurrentHashMap<>();
    //客户端启动状态 0初始化中 1初始化完成 2停止中
    public static AtomicInteger initState = new AtomicInteger(0);
    // 客户端断开是否重连
    private static volatile boolean whetherReconnect;
    private final Bootstrap bootstrap = new Bootstrap();
    private final EventLoopGroup eventLoopGroupWorker;
    private final CyclicBarrier queueFullLock = new CyclicBarrier(3);
    /**
     * 发送同步消息时阻塞当前线程计数器
     */
    private final ThreadLocal<CountDownLatch> syncMessageControl = ThreadLocal.withInitial(new Supplier<CountDownLatch>() {
        @Override
        public CountDownLatch get() {
            return new CountDownLatch(1);
        }
    });
    private Map<String, Thread> syncThreadMap = new ConcurrentHashMap<>();
    // 同步消息缓存
    private Map<String, Message> syncMessageMap = new ConcurrentHashMap<>();
    private ApplicationContext context;
    private ConnectionInitConfiguration constants;
    /*****  消费者相关属性  ****/
    private Map<String, LinkedBlockingQueue<PushMessage>> messageToQueueMap = new ConcurrentHashMap();
    private Map<String, String> consumerAnnotationInfo;

    public NettyInvokeClientImpl(ConnectionInitConfiguration constants, Map<String, String> consumerAnnotationInfo,
                                 ApplicationContext context) {
        this.eventLoopGroupWorker = new NioEventLoopGroup(1, new ThreadFactory() {
            private final AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyClientSelector_%d", this.threadIndex.incrementAndGet()));
            }
        });
        this.constants = constants;
        this.consumerAnnotationInfo = consumerAnnotationInfo;
        this.context = context;
        // 启动客户端
        this.start();
        this.initQueueMap();
    }

    /**
     * 初始化方法，保证在{@link NettyClient}启动时调用此方法完成客户端初始化
     * 当客户端掉线时，通过{@link ProtoStuffEncodeClientHandler#channelInactive(ChannelHandlerContext)}在接受到通道关闭事件的时候去重启客户端
     */
    public void start() {
        // bootstrap 可重用, 只需在NettyClient实例化的时候初始化即可.
        bootstrap.group(eventLoopGroupWorker)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //加入处理器
                        ch.pipeline()
                                .addLast(new IdleStateHandler(0, 0, 60L, TimeUnit.SECONDS))
                                .addLast(new MsgPackEncoder())
                                .addLast(new MsgPackDecoder())
                                .addLast(new LifeCycleInBoundHandler(NettyInvokeClientImpl.this));
                    }
                });
        boolean whetherSetSuccess = initState.compareAndSet(0, 1);
        if (!whetherSetSuccess) {
            throw new BusinessException(ClientBizErrorInfo.UNKNOWN, "Netty客户端启动失败！");
        }
        try {
            cacheChannel(constants.getHost(), constants.getPort(), null);
        } catch (InterruptedException e) {
            logger.error("客户端启动netty缓存channel失败！", e);
        }
    }

    /**
     * 初始化本地队列
     */
    private void initQueueMap() {
        consumerAnnotationInfo.values().forEach((queueName) -> {
            messageToQueueMap.put(queueName, new LinkedBlockingQueue());
        });
    }

    @Override
    public ResultWrapper sendSync(Object requestMsg) {
        ResultWrapper result = new ResultWrapper();
        String threadId = Thread.currentThread().getName();
        Message syncMessage = null;
        try {
            if (this.initState.get() != 1) {
                throw new BusinessException(ClientBizErrorInfo.INVALID_PARAM, "Netty客户端状态错误！");
            }
            //从缓存中获取；没有则创建新的
            Channel channel = getChannel(constants.getHost(), constants.getPort());
            if (channel != null && channel.isActive()) {
                SocketAddress address = channel.remoteAddress();
                if (channel.writeAndFlush(requestMsg).sync().isSuccess()) {
                    result.setNetState(Boolean.TRUE);
                    result.setBusyState(Boolean.TRUE);
                    logger.info("向{}同步发送消息成功！", JSON.toJSONString(address));
                }
            } else {
                channel.close().addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        logger.info("closeChannel: close the connection to remote address[{}:{}] result: {}", constants.getHost(), constants.getPort(),
                                future.isSuccess());
                    }
                });
                channelCache.remove(constants.getPort() + ":" + constants.getHost());
            }
            // 同步阻塞等待消息返回, 最长阻塞5秒钟
            syncThreadMap.put(threadId, Thread.currentThread());
            LockSupport.parkNanos(1000 * 1000 * 5L);
            syncMessage = syncMessageMap.get(threadId);
        } catch (TimeoutException | InterruptedException se) {
            result.setNetState(false);
            logger.error("发送消息发生网络异常！", se);
        } finally {
            result.setResult(syncMessage);
            return result;
        }
    }

    /**
     * 异步通讯
     *
     * @param requestMsg
     * @return
     */
    @Override
    public ResultWrapper sendAsync(Object requestMsg) {
        ResultWrapper result = new ResultWrapper();
        try {
            if (this.initState.get() != 1) {
                throw new BusinessException(ClientBizErrorInfo.INVALID_PARAM, "Netty客户端状态错误！");
            }
            //从缓存中获取；没有则创建新的
            Channel channel = getChannel(constants.getHost(), constants.getPort());
            if (channel != null && channel.isActive()) {
                SocketAddress address = channel.remoteAddress();
                channel.writeAndFlush(requestMsg).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if (channelFuture.isSuccess()) {
//                            logger.info("给{}发送消息成功！", address);
                        } else {
                            logger.info("给{}发送消息失败！", address);
                        }
                    }
                });
                // 异步发送消息直接返回true
                result.setNetState(true);
                result.setBusyState(true);
            } else {
                channel.close().addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        logger.info("closeChannel: close the connection to remote address[{}:{}] result: {}", constants.getHost(), constants.getPort(),
                                future.isSuccess());
                    }
                });
                channelCache.remove(constants.getHost() + ":" + constants.getPort());
            }
        } catch (TimeoutException | InterruptedException se) {
            result.setNetState(false);
            logger.error("发送消息发生网络异常！", se);
        }
        return result;
    }

    /**
     * 获取和服务端绑定的channel
     *
     * @return
     */
    private Channel getChannel(String host, String port) throws InterruptedException {
        Channel channel = channelCache.get(host + ":" + port);
        // 缓存中没有的话重新建立连接
        if (null == channel || !channel.isActive()) {
            InetSocketAddress isa = new InetSocketAddress(host, Integer.parseInt(port));
            channel = this.bootstrap.connect(isa).channel();
            if (channel.isActive()) {
                channelCache.put(host + ":" + port, channel);
            }
        }
        return channel;
    }

    /**
     * netty启动完毕后缓存channel；否则第一次连接一个服务端会耗时一定时间
     *
     * @return
     */
    private void cacheChannel(String host, String port, Channel channel) throws InterruptedException {
        if (null == channel) {
            InetSocketAddress isa = new InetSocketAddress(host, Integer.parseInt(port));
            ChannelFuture channelFuture = this.bootstrap.connect(isa).sync();
            channel = channelFuture.channel();
            if (channel == null || !channel.isActive()) {
                throw new BusinessException(ClientBizErrorInfo.UNKNOWN, "缓存channel连接异常！");
            }
        }
        //放入最新生成的channel
        channelCache.put(host + ":" + port, channel);
    }

    /**
     * 客户端重连
     *
     * @return
     */
    public void connectAsync() {
        if (!constants.getWhetherReconnect()) {
            logger.info("系统配置默认不重连");
            return;
        }
        logger.info("尝试异步重连到服务端");
        ChannelFuture channelFuture = bootstrap.connect(constants.getHost(), Integer.parseInt(constants.getPort()));
        channelFuture.addListener((ChannelFutureListener) future -> {
            Throwable cause = future.cause();
            if (cause != null) {
                logger.info("等待下一次重连,失败原因为{}, {}", cause.getMessage(), cause);
                channelFuture.channel().eventLoop().schedule(this::connectAsync, 5, TimeUnit.SECONDS);
            } else {
                Channel channel = channelFuture.channel();
                if (channel != null && channel.isActive()) {
                    cacheChannel(constants.getHost(), constants.getPort(), channel);
                    logger.info("Netty client started !!! {} connect to server", channel.localAddress());
                }
            }
        });
    }

    @Override
    public ConnectionInitConfiguration getClientConfiguration() {
        return this.constants;
    }

    /**
     * 将拉取的消息存到本地队列中
     *
     * @param list
     */
    public void insertMessagesToBlockQueue(List<PushMessage> list, String queueName) {
        LinkedBlockingQueue queueToThisQueueName = getQueueByQueueName(queueName);
        boolean whetherPublish = queueToThisQueueName.size() == 0;
        for (PushMessage pushMessage : list) {
            try {
                queueToThisQueueName.offer(pushMessage, 3L, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.error("本地队列已满；{}", e);
                // todo 应该放到本地消息文件中或者数据库中
            }
        }
        if (whetherPublish) {
            AtomicReference<String> beanName = new AtomicReference<>();
            consumerAnnotationInfo.entrySet().forEach((entry) -> {
                if (queueName.equals(entry.getValue())) {
                    beanName.set(entry.getKey());
                }
            });
            context.publishEvent(new MessagePullEvent(beanName));
        }
    }

    /**
     * 获取消费线程池对应的阻塞队列
     *
     * @param queueName
     * @return {@link PullMessage}
     */
    public LinkedBlockingQueue getQueueByQueueName(String queueName) {
        LinkedBlockingQueue queueToThisQueueName = messageToQueueMap.get(queueName);
        if (queueToThisQueueName == null) {
            return messageToQueueMap.put(queueName, new LinkedBlockingQueue());
        }
        return queueToThisQueueName;
    }

    /**
     * 获取消费者对应队列名
     *
     * @param beanName
     * @return {@link PullMessage}
     */
    public String getQueueName(String beanName) {
        String queueName = consumerAnnotationInfo.get(beanName);
        if (StringUtil.isNullOrEmpty(queueName)) {
            throw new BusinessException(ClientBizErrorInfo.UNKNOWN, "创建消费者监听失败！");
        }
        return queueName;
    }

    /**
     * 根据队列名获取单条消息
     *
     * @param queueName
     * @return
     */
    @Override
    public Object getSingleMessage(String queueName) {
        Object message = getQueueByQueueName(queueName).poll();
        return message;
    }

    /**
     * 传递netty收到的同步信息到同步信息Map中并唤醒同步等待的线程
     *
     * @param message
     * @param threadId
     * @return
     */
    public void passSyncMessage(String threadId, Message message) {
        if (Strings.isNotBlank(threadId)) {
            syncMessageMap.put(threadId, message);
        }
        // 唤醒同步线程, 并删除等待线程map中的thread数据
        Thread lockThread = syncThreadMap.get(threadId);
        if (lockThread != null && lockThread.isAlive()) {
            LockSupport.unpark(syncThreadMap.get(threadId));
            syncThreadMap.remove(threadId);
        }
    }
}
