package com.xshmq.server.handler;

import com.xshmq.server.entity.Message;
import com.xshmq.util.ProtostuffUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 消息prostuff编码类
 */
public class MsgPackEncoder extends MessageToByteEncoder<Message> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message msg, ByteBuf out) throws Exception {
        byte[] serializeByte = ProtostuffUtil.serializer(msg);
        // 写入包的的长度
        out.writeInt( serializeByte.length );
        // 写入消息主体
        out.writeBytes( Unpooled.copiedBuffer(serializeByte) );
    }
}
