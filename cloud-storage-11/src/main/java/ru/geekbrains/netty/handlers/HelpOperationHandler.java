package ru.geekbrains.netty.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.geekbrains.netty.commands.ServerCommand;

import java.util.Arrays;

public class HelpOperationHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = String.valueOf(msg);

        if (message.startsWith(ServerCommand.HELP_COMMANDS.getName())) {
            Arrays.stream(ServerCommand.values()).map(ServerCommand::toString).forEach(ctx::write);
        }

        ctx.fireChannelRead(msg);
    }
}
