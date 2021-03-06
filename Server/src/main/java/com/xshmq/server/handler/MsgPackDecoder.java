package com.xshmq.server.handler;

import com.xshmq.NettyServer;
import com.xshmq.server.entity.Message;
import com.xshmq.util.ProtostuffUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 消息prostuff解码
 */
public class MsgPackDecoder extends ByteToMessageDecoder {

    private final static Logger logger = LoggerFactory.getLogger(NettyServer.class);

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        // 消息的长度
        if (byteBuf.readableBytes() < 4) {
            logger.error("需要解码的消息长度异常！");
        }
        int length = byteBuf.readInt();

        //读取content
        byte[] data = new byte[length];
        byteBuf.readBytes(data);

        Message message = ProtostuffUtil.deserializer(data, Message.class);
        list.add( message );
        byteBuf.discardReadBytes();//回收已读字节

    }
}
