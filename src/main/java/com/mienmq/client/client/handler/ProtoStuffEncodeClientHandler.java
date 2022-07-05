package com.mienmq.client.client.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * protostuff解码
 */

public class ProtoStuffEncodeClientHandler extends SimpleChannelInboundHandler {
    private static final Logger logger = LoggerFactory.getLogger(SimpleChannelInboundHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        logger.info("服务端的地址： " + channelHandlerContext.channel().remoteAddress());
        logger.info("收到服务器发往此客户端消息:" + JSON.toJSONString(o));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //测试用protostuff对对象编解码
//        ByteBuf buf = Unpooled.copiedBuffer(ProtostuffUtil.serializer(new User(1, "zhuge")));
//        ctx.writeAndFlush(buf);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("与服务器断开连接服务器");
    }

    /** 服务端直接断开需要正常关闭，不然会直接报错导致重连失败 **/
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
