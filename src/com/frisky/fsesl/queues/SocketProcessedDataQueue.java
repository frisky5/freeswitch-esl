package com.frisky.fsesl.queues;

import io.netty.buffer.ByteBuf;

import java.util.concurrent.LinkedBlockingQueue;

public class SocketProcessedDataQueue {
    private static LinkedBlockingQueue<String> socketProcessedDataQueue = null;

    private SocketProcessedDataQueue() {

    }

    public static LinkedBlockingQueue<String> getQueue() {
        if (socketProcessedDataQueue == null)
            socketProcessedDataQueue = new LinkedBlockingQueue<>();
        return socketProcessedDataQueue;
    }
}
