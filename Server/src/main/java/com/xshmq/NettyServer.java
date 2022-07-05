package com.xshmq;

import com.xshmq.server.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class NettyServer {
    private final static Logger logger = LoggerFactory.getLogger(NettyServer.class);

    public static void main(String[] args) throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new MsgPackEncoder())
                                    .addLast(new MsgPackDecoder())
                                    .addLast(new LifeCycleInBoundHandler())
                                    .addLast(new IdleStateHandler(0, 0, 30L, TimeUnit.SECONDS ))
                                    .addLast(new HeartBeatServerHandler())
                            ;
                        }
                    });

            // 服务端绑定端口 .sync()是指等待执行完；需要添加监听器的话必须是异步的
            ChannelFuture channelFuture = serverBootstrap.bind(9000);
            // 给cf注册监听器，监听我们关心的事件
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (channelFuture.isSuccess()) {
                        logger.info("监听端口9000成功");
                    } else {
                        logger.info("监听端口9000失败");
                    }
                }
            });
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
