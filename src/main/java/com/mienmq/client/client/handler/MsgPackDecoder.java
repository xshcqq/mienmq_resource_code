package com.mienmq.client.client.handler;

import com.mienmq.client.client.service.base.Message;
import com.mienmq.client.util.ProtostuffUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * 消息prostuff解码
 */
public class MsgPackDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        // 消息的长度
        int length = byteBuf.readInt();

        //读取content
        byte[] data = new byte[length];
        byteBuf.readBytes( data );

        Message message = ProtostuffUtil.deserializer(data, Message.class);
        list.add( message );
        byteBuf.discardReadBytes();//回收已读字节

    }
}
