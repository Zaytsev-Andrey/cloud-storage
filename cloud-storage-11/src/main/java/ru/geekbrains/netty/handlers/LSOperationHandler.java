package ru.geekbrains.netty.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.geekbrains.netty.commands.ServerCommand;
import ru.geekbrains.netty.entities.Client;
import ru.geekbrains.netty.storage.SimpleStorage;

import java.net.SocketAddress;

public class LSOperationHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
        if (s.startsWith(ServerCommand.LS_COMMAND.getName())) {
            SocketAddress clientAddress = channelHandlerContext.channel().remoteAddress();
            Client client = SimpleStorage.getClient(clientAddress);

            String fileList = String.join(" ", client.getPath().toFile().list());
            channelHandlerContext.write(fileList.concat(System.lineSeparator()));
        }
        channelHandlerContext.fireChannelRead(s);
    }
}
