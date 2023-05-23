package com.fileStorage.frontend.network;

import com.fileStorage.common.ByteObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class Handler extends SimpleChannelInboundHandler<ByteObject> {
    private final CallbackImpl callback = new CallbackImpl();
    @Override
    protected void channelRead0(ChannelHandlerContext context, ByteObject object) {
        callback.onReceived(object);
    }
}


