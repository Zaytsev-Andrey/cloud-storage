package ru.geekbrains.netty.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.geekbrains.netty.entities.Client;
import ru.geekbrains.netty.storage.SimpleStorage;

public class PromptHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Client client = SimpleStorage.getClient(ctx.channel().remoteAddress());
        String path = client.getPath().equals(SimpleStorage.ROOT_PATH) ? "~" : client.getPath().getFileName().toString();
        String prompt = String.format("[%s %s] ", client.getNickname(), path);
        ctx.writeAndFlush(prompt);
    }
}
