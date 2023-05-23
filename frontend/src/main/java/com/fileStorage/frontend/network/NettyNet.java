package com.fileStorage.frontend.network;

import com.fileStorage.common.ByteObject;
import com.fileStorage.frontend.constants.Constants;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyNet {
    private SocketChannel channel;
    public NettyNet() {
        new Thread(() -> {
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                ChannelFuture future = bootstrap.channel(NioSocketChannel.class)
                        .group(group)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) {
                                channel = socketChannel;
                                channel.pipeline().addLast(
                                        new ObjectEncoder(),
                                        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                        new Handler()
                                );
                            }
                        }).connect(Constants.SERVER_ADDRESS, Constants.SERVER_PORT).sync();
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                log.error("Exception = ", e);
            } finally {
                group.shutdownGracefully();
            }
        }).start();
    }

    public void sendObject(ByteObject object) {
        log.debug(channel.toString());
        channel.writeAndFlush(object);
    }
}
