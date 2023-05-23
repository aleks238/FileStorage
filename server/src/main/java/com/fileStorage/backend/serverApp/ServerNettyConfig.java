package com.fileStorage.backend.serverApp;

import com.fileStorage.backend.authService.AuthUsers;
import com.fileStorage.backend.constants.Constants;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class ServerNettyConfig {
    public static void main(String[] args) {
        /*Создание корневой директории в которой будут директории клиентов для хранения их файлов */
        try {
            //Path cloudUsers = Paths.get("cloudUsers");
            Path cloudUsers = Paths.get("server/users");
            if (Files.notExists(cloudUsers)){
                Files.createDirectory(cloudUsers);
            }
        }catch (IOException e){
            log.error("e=", e);
        }
        /*Настройка сервера */
        EventLoopGroup auth = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();
        AuthUsers authUsers = new AuthUsers();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            ChannelFuture future = bootstrap.group(auth, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(
                                    new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                    new ObjectEncoder(),
                                    new ClientHandler(authUsers)
                            );
                        }
                    })
                    .bind(Constants.SERVER_PORT).sync();
            log.debug("Сервер открыт...");
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("e=", e);
        } finally {
            auth.shutdownGracefully();
            worker.shutdownGracefully();
            authUsers.closeConnection();
        }
    }


}
