package com.mienmq.client.client;

import com.mienmq.client.client.handler.LifeCycleInBoundHandler;
import com.mienmq.client.client.handler.ProtoStuffEncodeClientHandler;
import com.mienmq.client.client.service.impl.NettyInvokeClientImpl;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class NettyClient {
    private final static Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private final ThreadPoolExecutor initExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(100));
    private String host;
    private int port;
    private Bootstrap bootstrap;
    private EventLoopGroup group;
    private ConnectRunner connectRunner;
    //客户端启动状态 0初始化中 1初始化完成 2停止中
    public static AtomicInteger initState = new AtomicInteger(0);

    public NettyClient(String host, int port, boolean whetherConnect) {
        this.host = host;
        this.port = port;
        //创建客户端启动对象
        this.bootstrap = new Bootstrap();
        //客户端需要一个事件循环组
        this.group = new NioEventLoopGroup();
        this.connectRunner = new ConnectRunner(whetherConnect);
        init();
    }

    /**
     * 初始化方法，保证在{@link NettyClient}启动时调用此方法完成客户端初始化
     * 当客户端掉线时，通过{@link ProtoStuffEncodeClientHandler}中的channelInactive()方法在接受到通道关闭事件的时候去重启客户端
     */
    private void init() {
        // bootstrap 可重用, 只需在NettyClient实例化的时候初始化即可.
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //加入处理器
                        ch.pipeline()
                                .addLast(new ProtoStuffEncodeClientHandler())
                                .addLast(new IdleStateHandler(0, 0, 30L, TimeUnit.SECONDS ));
                    }
                });
    }

    /**
     * 开启独立线程启动客户端
     */
    public void initClient() {
        initExecutor.execute(new ConnectRunner(connectRunner.whetherReconnect));
    }

    public class ConnectRunner implements Runnable {
        // 客户端断开是否重连
        private volatile boolean whetherReconnect;

        public boolean isWhetherReconnect() {
            return whetherReconnect;
        }
        //可改变重连策略；是否重连
        public void setWhetherReconnect(boolean whetherReconnect) {
            this.whetherReconnect = whetherReconnect;
        }

        public ConnectRunner(boolean whetherReconnect) {
            this.whetherReconnect = whetherReconnect;
        }

        @Override
        public void run() {
            try {
                if (0 == initState.get()) {
                    synchronized (initState) {
                        if (0 == initState.get()) {
                            logger.info("netty client will start。。");
                            //启动客户端去连接服务器端
                            ChannelFuture cf = bootstrap.connect(host, port);
                            cf.addListener(new ChannelFutureListener() {
                                @Override
                                public void operationComplete(ChannelFuture future) throws Exception {
                                    if (!future.isSuccess() && whetherReconnect) {
                                        //重连交给后端线程执行
                                        future.channel().eventLoop().schedule(() -> {
                                            logger.info("重连服务端...");
                                            try {
                                                initClient();
                                            } catch (Exception e) {
                                                logger.error("重连服务端失败...", e);
                                                // do something
                                            }
                                        }, 3000, TimeUnit.MILLISECONDS);
                                    } else {
                                        logger.info("服务端连接成功...");
                                    }
                                }
                            });
                            //对通道关闭进行监听
                            cf.channel().closeFuture().sync();
                            //初始化完成
                            initState.compareAndSet(0, 1);
                        }
                    }
                }
            }catch (InterruptedException e) {
                logger.error("netty关闭通道监听异常！", e);
            } finally {
                group.shutdownGracefully();
            }
        }
    }
}
