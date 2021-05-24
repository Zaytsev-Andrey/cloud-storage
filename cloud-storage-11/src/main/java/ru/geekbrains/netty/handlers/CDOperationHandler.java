package ru.geekbrains.netty.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.geekbrains.netty.commands.ServerCommand;
import ru.geekbrains.netty.entities.Client;
import ru.geekbrains.netty.storage.SimpleStorage;

import java.lang.annotation.Target;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class CDOperationHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = String.valueOf(msg);

        if (message.startsWith(ServerCommand.CD_COMMAND.getName())) {
            String[] command = message.split("\\s+");
            if (command.length >= 2) {
                Client client = SimpleStorage.getClient(ctx.channel().remoteAddress());
                Path currentPath = client.getPath();
                Path targetPath = Path.of(currentPath.toString(), command[1]);

                if ("~".equals(command[1])) {
                    client.setPath(SimpleStorage.ROOT_PATH);
                } else if ("..".equals(command[1])) {
                    if (currentPath.getParent() != null) {
                        client.setPath(currentPath.getParent());
                    }
                } else if (Files.exists(targetPath)) {
                    client.setPath(targetPath);
                }
            }
        }

        ctx.fireChannelRead(msg);
    }
}
