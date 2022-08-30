package com.mienmq.client.client.handler;

import com.mienmq.client.client.service.base.Message;
import com.mienmq.client.util.ProtostuffUtil;
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
    private final static Logger logger = LoggerFactory.getLogger(MsgPackDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        try {
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
        } catch (Exception e) {
//            logger.error("服务端读取数据发生异常", e);
            // 跳过此次异常的数据包
            byteBuf.skipBytes(byteBuf.readableBytes());
        } finally {
            byteBuf.discardReadBytes();//回收已读字节
        }

    }
}
