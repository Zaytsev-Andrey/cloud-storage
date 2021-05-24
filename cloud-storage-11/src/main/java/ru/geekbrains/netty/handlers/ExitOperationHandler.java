package ru.geekbrains.netty.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.geekbrains.netty.commands.ServerCommand;


public class ExitOperationHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = String.valueOf(msg);

        if (message.startsWith(ServerCommand.EXIT_COMMAND.getName())) {
            ctx.close();
        }

        ctx.fireChannelRead(msg);
    }
}
