package ru.geekbrains.netty.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.geekbrains.netty.commands.ServerCommand;
import ru.geekbrains.netty.entities.Client;
import ru.geekbrains.netty.storage.SimpleStorage;

import java.nio.file.Files;
import java.nio.file.Path;

public class ChangeNicknameOperationHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = String.valueOf(msg);

        if (message.startsWith(ServerCommand.CHANGE_NICKNAME.getName())) {
            String[] command = message.split("\\s+");

            if (command.length >= 2) {
                Client client = SimpleStorage.getClient(ctx.channel().remoteAddress());
                client.setNickname(command[1]);
            }
        }

        ctx.fireChannelRead(msg);
    }
}
