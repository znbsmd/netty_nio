package com.zjn.nettyrpc;

import com.zjn.nettyrpc.service.impl.HelloServerImpl;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class RpcServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 如何符合约定，则调用本地方法，返回数据
        if (msg.toString().startsWith(RpcClient.providerName)) {
            String result = new HelloServerImpl()
                    .hello(msg.toString().substring(msg.toString().lastIndexOf("#") + 1));
            ctx.writeAndFlush(result);
        }

    }
}
