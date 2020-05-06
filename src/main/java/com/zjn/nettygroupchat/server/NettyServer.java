package com.zjn.nettygroupchat.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class NettyServer {

    public static void main(String[] args) throws Exception{

        // 多Reactor多线程模型 创建主线程组（只负责处理连接）
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        // 创建从线程组（负责处理业务）
        NioEventLoopGroup work = new NioEventLoopGroup();//8个NioEventLoop
        try {
            // 创建启动类
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            serverBootstrap = serverBootstrap.group(boss, work)
                    .channel(NioServerSocketChannel.class)
                    // 默认就是128
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    // 保持长链接
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) throws Exception {
                            ChannelPipeline pipeline = sc.pipeline();
                            // 解码器
                            pipeline.addLast(new StringDecoder());
                            // 编码器
                            pipeline.addLast(new StringEncoder());
                            // 业务处理
                            pipeline.addLast(new ServerHandler());
                        }
                    });

            ChannelFuture channelFuture = serverBootstrap.bind(7001).sync();

            // 监听关闭后服务端也停止
            channelFuture.channel().closeFuture().sync();
        }finally {

            boss.shutdownGracefully();
            work.shutdownGracefully();
        }

    }
}
