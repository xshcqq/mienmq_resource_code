package com.xshmq.server.handler;

import com.alibaba.fastjson.JSON;
import com.xshmq.server.entity.ListSchema;
import com.xshmq.server.entity.Message;
import com.xshmq.server.entity.PullMessage;
import com.xshmq.server.entity.PushMessage;
import com.xshmq.server.enums.RequestType;
import com.xshmq.server.enums.ServerBizErrorInfo;
import com.xshmq.server.exception.ServerException;
import com.xshmq.util.ProtostuffUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static com.xshmq.server.enums.RequestType.*;

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
    private final static Map<String, LinkedBlockingQueue<byte[]>> messageQueues = new ConcurrentHashMap<>(16);

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
    public void channelInactive(ChannelHandlerContext ctx) 
            throws Exception {
        logger.info("channelInactive: channel被关闭");
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        logger.info("channelRead: channel中有可读的数据" );
        Message message = (Message) msg;

        // 根据不同的请求类型执行不同的业务逻辑
        RequestType type = getTypeByEnumName(message.getRequestType());
        switch (type) {
                // 发送消息
            case SEND_MESSAGE:
                String queueName = message.getQueueName();
                if (StringUtil.isNullOrEmpty(queueName)) {
                    throw new ServerException(ServerBizErrorInfo.PULL_MESSAGE_ERROR, "拉取服务端消息异常，消息队列为空！");
                }
                if (messageQueues.containsKey(queueName)){
                    messageQueues.get(queueName).put(message.getContent());
                } else {
                    LinkedBlockingQueue messages = new LinkedBlockingQueue();
                    messages.put(message.getContent());
                    messageQueues.put(queueName, messages);
                }
                break;
                // 拉取消息
            case PULL_MESSAGE:
                logger.info("收到拉取消息请求");
                // 队列名
                String pullQueueName = message.getQueueName();
                // 获取队列中消息的条数
                PullMessage pullMessage = ProtostuffUtil.deserializer(message.getContent(), PullMessage.class);
                int messageNum = pullMessage.getMessageNum();
                List<PushMessage> messageList = null;
                if (messageQueues.containsKey(pullQueueName)){
                    LinkedBlockingQueue<byte[]> messages = messageQueues.get(pullQueueName);
                    // 队列中消息数量不够请求拉取数量时；取消息队列的size
                    messageNum = (messages.size() > messageNum) ? messageNum : messages.size();
                    if (messages.size() >0){
                        for (int i = 0; i < messageNum; i++) {
                            messageList = new LinkedList<PushMessage>();
                            // poll() 队列中消息数量不足还是会返回null
                            byte[] poll = messages.poll();
                            if (poll.length != 0 && poll != null){
                                PushMessage pushMessage = ProtostuffUtil.deserializer(poll, PushMessage.class);
                                messageList.add(pushMessage);
                            }
                        }
                    }
                } else {
                    LinkedBlockingQueue messages = new LinkedBlockingQueue();
                    messages.put(message.getContent());
                    messageQueues.put(pullQueueName, messages);
                }
                // 将从队列中取出的消息回传给客户端
                if (null != messageList && messageList.size() > 0) {
                    Message respMessage = new Message();
                    respMessage.setMessageId(UUID.randomUUID().toString());
                    respMessage.setRequestType(PULL_MESSAGE.name());
                    respMessage.setQueueName(message.getQueueName());
                    respMessage.setContent(ProtostuffUtil.serializer(new ListSchema(messageList)));
                    ctx.writeAndFlush(respMessage);
                    logger.info("拉取到消息【{}】条", messageList.size());
                }
                break;
            case HEART_BEAT:
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + message.getRequestType());
        }
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
}