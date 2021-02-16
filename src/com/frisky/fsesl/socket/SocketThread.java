package com.frisky.fsesl.socket;

import com.frisky.fsesl.EslConnector;
import com.frisky.fsesl.threadControl.ThreadControl;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketThread implements Runnable {
    private ThreadControl threadControl;

    public SocketThread(ThreadControl _threadControl) {
        this.threadControl = _threadControl;

    }

    @Override
    public void run() {
        threadControl.setIsSocketThreadRunning(true);
        EventLoopGroup nioEventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            AtomicBoolean isConnected = new AtomicBoolean(false);
            bootstrap.group(nioEventLoopGroup);
            bootstrap.channel(NioSocketChannel.class);

            bootstrap.remoteAddress(new InetSocketAddress(threadControl.getProperty(EslConnector.FS_ESL_IP_ADDRESS), Integer.parseInt(threadControl.getProperty(EslConnector.FS_ESL_PORT))));
            bootstrap.handler(new ChannelInit(threadControl));
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.option(ChannelOption.SO_LINGER, 0);

            ChannelFuture channelFuture = bootstrap.connect().sync();
            Thread writingThread = new Thread(new WritingThread(channelFuture.channel(), threadControl));
            writingThread.start();
            isConnected.set(true);
            channelFuture.channel().closeFuture().sync();
            isConnected.set(false);
        } catch (InterruptedException e) {
            threadControl.setIsSocketConnected(false);
            e.printStackTrace();
        } finally {
            threadControl.setIsSocketConnected(false);
            threadControl.setIsSocketThreadRunning(false);
            try {
                nioEventLoopGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
