package ru.geekbrains.netty.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.geekbrains.netty.commands.ServerCommand;
import ru.geekbrains.netty.entities.Client;
import ru.geekbrains.netty.storage.SimpleStorage;

import java.nio.file.Files;
import java.nio.file.Path;

public class MKDIROperationHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = String.valueOf(msg);

        if (message.startsWith(ServerCommand.MKDIR_COMMAND.getName())) {
            String[] command = message.split("\\s+");

            if (command.length >= 2) {
                Client client = SimpleStorage.getClient(ctx.channel().remoteAddress());
                Path currentPath = client.getPath();
                Path targetPath = Path.of(currentPath.toString(), command[1]);

                if (!Files.exists(targetPath)) {
                    Files.createDirectories(targetPath);
                } else {
                    String errMsg = String.format("mkdir: cannot create directory '%s' : File exists%n", command[1]);
                    ctx.write(errMsg);
                }
            }
        }

        ctx.fireChannelRead(msg);
    }
}
