package com.mienmq.client.client.handler;

import com.alibaba.fastjson.JSON;
import com.mienmq.client.client.service.base.ListSchema;
import com.mienmq.client.client.service.base.Message;
import com.mienmq.client.client.service.base.PullMessage;
import com.mienmq.client.client.service.base.PushMessage;
import com.mienmq.client.client.service.impl.NettyInvokeClientImpl;
import com.mienmq.client.enums.RequestType;
import com.mienmq.client.util.ProtostuffUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 *  handler的生命周期回调接口调用顺序:
 *  handlerAdded -> channelRegistered -> channelActive -> channelRead -> channelReadComplete
 *  -> channelInactive -> channelUnRegistered -> handlerRemoved
 *
 * handlerAdded: 新建立的连接会按照初始化策略，把handler添加到该channel的pipeline里面，也就是channel.pipeline.addLast(new LifeCycleInBoundHandler)执行完成后的回调；
 * channelRegistered: 当该连接分配到具体的worker线程后，该回调会被调用。
 * channelActive：channel的准备工作已经完成，所有的pipeline添加完成，并分配到具体的线上上，说明该channel准备就绪，可以使用了。
 * channelRead：客户端向服务端发来数据，每次都会回调此方法，表示有数据可读；
 * channelReadComplete：服务端每次读完一次完整的数据之后，回调该方法，表示数据读取完毕；
 * channelInactive：当连接断开时，该回调会被调用，说明这时候底层的TCP连接已经被断开了。
 * channelUnRegistered: 对应channelRegistered，当连接关闭后，释放绑定的workder线程；
 * handlerRemoved： 对应handlerAdded，将handler从该channel的pipeline移除后的回调方法。
 */
public class LifeCycleInBoundHandler extends ChannelInboundHandlerAdapter {
    
    private final static Logger logger = LoggerFactory.getLogger(LifeCycleInBoundHandler.class);

    private NettyInvokeClientImpl invokeClient;

    public LifeCycleInBoundHandler(NettyInvokeClientImpl invokeClient) {
        this.invokeClient = invokeClient;
    }
    @Override
    public void channelRegistered(ChannelHandlerContext ctx)
            throws Exception {
        logger.info("channelRegistered: channel注册到NioEventLoop");
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) 
            throws Exception {
        logger.info("channelUnregistered: channel取消和NioEventLoop的绑定");
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) 
            throws Exception {
        logger.info("channelActive: channel准备就绪");
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) 
            throws Exception {
        Message message = (Message)msg;

        RequestType type = RequestType.getTypeByEnumName(message.getRequestType());
        switch (type) {
                // 发送消息
            case SEND_MESSAGE:
                break;
                // 拉取消息
            case PULL_MESSAGE:
                // 获取队列中消息的条数
                ListSchema pullMessageStr = ProtostuffUtil.deserializer(message.getContent(), ListSchema.class);
                List<PushMessage> pullMessages = pullMessageStr.getPushMessages();
                // 队列名
                String pullQueueName = message.getQueueName();
                // 将拉取的消息放到本地队列
                invokeClient.insertMessagesToBlockQueue(pullMessages, pullQueueName);
                break;

        }
        logger.info("channelRead: channel中有可读的数据:{}", JSON.toJSONString(message));
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) 
            throws Exception {
        logger.info("channelReadComplete: channel读数据完成");
        super.channelReadComplete(ctx);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) 
            throws Exception {
        logger.info("handlerAdded: handler被添加到channel的pipeline");
        super.handlerAdded(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) 
            throws Exception {
        logger.info("handlerRemoved: handler从channel的pipeline中移除");
        super.handlerRemoved(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.warn("channelInactive:{}", ctx.channel().localAddress());
        // 避免重复调用exceptionCaught()方法，会调用两次reconnect()
        ctx.pipeline().remove(this);
        ctx.channel().close();
        reconnectionAsync(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof IOException) {
            logger.warn("exceptionCaught:客户端[{}]和远程断开连接", ctx.channel().localAddress());
        } else {
            logger.error("未知错误导致客户端可服务端断开连接{}", cause);
        }
        ctx.pipeline().remove(this);
        ctx.close();
        reconnectionAsync(ctx);
    }

    private void reconnectionAsync(ChannelHandlerContext ctx) {
        logger.info("3s之后重新建立连接");
        ctx.channel().eventLoop().schedule(new Runnable() {
            @Override
            public void run() {
                invokeClient.connectAsync();
            }
        }, 3, TimeUnit.SECONDS);
    }
}