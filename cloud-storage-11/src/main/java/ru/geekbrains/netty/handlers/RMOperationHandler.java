package ru.geekbrains.netty.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.geekbrains.netty.commands.ServerCommand;
import ru.geekbrains.netty.entities.Client;
import ru.geekbrains.netty.storage.SimpleStorage;

import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class RMOperationHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = String.valueOf(msg);

        if (message.startsWith(ServerCommand.RM_COMMAND.getName())) {
            String[] command = message.split("\\s+");

            if (command.length >= 2) {
                Client client = SimpleStorage.getClient(ctx.channel().remoteAddress());
                Path currentPath = client.getPath();
                Path targetPath = Path.of(currentPath.toString(), command[1]);

                try {
                    Files.delete(targetPath);
                } catch (DirectoryNotEmptyException e) {
                    String errMsg = String.format("remove: cannot %s: Directory not empty%n",
                            String.join(" ", command));
                    ctx.write(errMsg);
                } catch (NoSuchFileException e) {
                    String errMsg = String.format("remove: cannot %s: No such file or directory%n",
                            String.join(" ", command));
                    ctx.write(errMsg);
                }
            }
        }

        ctx.fireChannelRead(msg);
    }
}
