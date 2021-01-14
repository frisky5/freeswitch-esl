package com.frisky.fsesl.socket;

import com.frisky.fsesl.threadControl.ThreadControl;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class ChannelInit extends ChannelInitializer<SocketChannel> {
    private ThreadControl threadControl;

    public ChannelInit(ThreadControl _threadControl) {
        this.threadControl = _threadControl;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast(new DataReadHandler(threadControl));
    }
}
