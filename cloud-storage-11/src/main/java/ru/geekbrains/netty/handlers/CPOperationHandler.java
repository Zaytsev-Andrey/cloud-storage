package ru.geekbrains.netty.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.geekbrains.netty.commands.ServerCommand;
import ru.geekbrains.netty.entities.Client;
import ru.geekbrains.netty.storage.SimpleStorage;

import java.nio.file.*;

public class CPOperationHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = String.valueOf(msg);

        if (message.startsWith(ServerCommand.CP_COMMAND.getName())) {
            String[] command = message.split("\\s+");

            if (command.length == 2) {
                String errMsg = String.format("cp: missing destination file operand after %s%n", command[1]);
                ctx.write(errMsg);
            } else if (command.length >= 3) {
                Client client = SimpleStorage.getClient(ctx.channel().remoteAddress());
                Path currentPath = client.getPath();
                Path sourcePath = Path.of(currentPath.toString(), command[1]);
                Path targetPath = Path.of(currentPath.toString(), command[2]);
                long sourceSize = 0;

                if (!Files.exists(sourcePath)) {
                    String errMsg = String.format("cp: cannot stat %s: Source no such file or directory%n", command[1]);
                    ctx.write(errMsg);
                } else {
                    // if source path it file or empty directory then copy
                    if (!Files.isDirectory(sourcePath) || sourcePath.toFile().list().length == 0) {
                        try {
                            sourceSize = Files.size(sourcePath);
                            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                            if (sourceSize != Files.size(targetPath)) {
                                String errMsg = String.format("cp: File %s: wasn't all copied%n",
                                        String.join(" ", command));
                                ctx.write(errMsg);
                            }
                        } catch (NoSuchFileException e) {
                            String errMsg = String.format("cp: cannot %s: Target no such file or directory%n",
                                    String.join(" ", command));
                            ctx.write(errMsg);
                        }
                    } else {
                        String errMsg = String.format("cp: cannot %s: Directory is not empty%n",
                                String.join(" ", command));
                        ctx.write(errMsg);
                    }
                }

            }
        }

        ctx.fireChannelRead(msg);
    }
}
