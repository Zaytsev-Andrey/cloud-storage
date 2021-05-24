package ru.geekbrains.netty.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.geekbrains.netty.storage.SimpleStorage;

import java.net.SocketAddress;

public class CloudStorageHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        SocketAddress address = ctx.channel().remoteAddress();
        SimpleStorage.addClient(address);
        System.out.println("Client connected " + address);

        String greeting = String.format("Hello client!%nEnter --help for support info%n");
        ctx.write(greeting);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        SocketAddress address = ctx.channel().remoteAddress();
        SimpleStorage.removeClient(address);
        System.out.println("Client disconnected " + address);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        StringBuilder sb = new StringBuilder();

        while (buf.isReadable()) {
            sb.append((char) buf.readByte());
        }

        ctx.fireChannelRead(sb.toString());
    }
}
