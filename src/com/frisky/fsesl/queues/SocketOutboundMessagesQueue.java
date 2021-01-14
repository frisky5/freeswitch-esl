package com.frisky.fsesl.queues;

import io.netty.buffer.ByteBuf;

import java.util.concurrent.LinkedBlockingQueue;

public class SocketOutboundMessagesQueue {
    private static LinkedBlockingQueue<String> socketOutboundMessagesQueue = null;

    private SocketOutboundMessagesQueue() {

    }

    public static LinkedBlockingQueue<String> getQueue() {
        if (socketOutboundMessagesQueue == null)
            socketOutboundMessagesQueue = new LinkedBlockingQueue<>();
        return socketOutboundMessagesQueue;
    }
}
