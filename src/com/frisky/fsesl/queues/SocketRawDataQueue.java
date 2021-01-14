package com.frisky.fsesl.queues;

import io.netty.buffer.ByteBuf;

import java.util.concurrent.LinkedBlockingQueue;

public class SocketRawDataQueue {

    private static LinkedBlockingQueue<String> socketRawDataQueue = null;

    private SocketRawDataQueue() {

    }

    public static LinkedBlockingQueue<String> getQueue() {
        if (socketRawDataQueue == null)
            socketRawDataQueue = new LinkedBlockingQueue<>();
        return socketRawDataQueue;
    }
}
