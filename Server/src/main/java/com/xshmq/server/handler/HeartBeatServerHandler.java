package com.xshmq.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

public class HeartBeatServerHandler extends SimpleChannelInboundHandler {
    private int times;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
    }

    //处理心跳检测事件的方法
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            String eventDesc = null;
            switch (event.state()) {
                case READER_IDLE:
                    eventDesc = "读空闲";
                    break;
                case WRITER_IDLE:
                    eventDesc = "写空闲";
                    break;
                case ALL_IDLE:
                    eventDesc = "读写空闲";
                    break;
            }
            System.out.println(ctx.channel().remoteAddress() + "发生超时事件--" + eventDesc);
            times++;
            if (times > 3) {
                System.out.println("空闲次数超过三次, 关闭连接");
                ctx.channel().close();
            }
        }
        super.userEventTriggered(ctx, evt);
    }
}
