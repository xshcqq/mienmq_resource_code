package com.mienmq.client.client.handler;

import com.mienmq.client.client.service.base.Message;
import com.mienmq.client.util.ProtostuffUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息prostuff编码类
 */
public class MsgPackEncoder extends MessageToByteEncoder<Message> {

    private final static Logger logger = LoggerFactory.getLogger(MsgPackEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message msg, ByteBuf out) throws Exception {
        byte[] serializeByte = ProtostuffUtil.serializer(msg);
        int messageLength = serializeByte.length;
        if (messageLength > 1024) {
            logger.error("需要编码的消息长度超长！");
        }
        // 写入包的的长度
        out.writeInt(messageLength);
        // 写入消息主体
//        out.writeBytes( Unpooled.copiedBuffer(serializeByte) );
        out.writeBytes(serializeByte);
    }
}
