package com.frisky.fsesl.queues;

import io.netty.buffer.ByteBuf;

import java.util.concurrent.LinkedBlockingQueue;

public class SocketRawDataQueue {

    private static LinkedBlockingQueue<byte[]> socketRawDataQueue = null;

    private SocketRawDataQueue() {

    }

    public static LinkedBlockingQueue<byte[]> getQueue() {
        if (socketRawDataQueue == null)
            socketRawDataQueue = new LinkedBlockingQueue<>();
        return socketRawDataQueue;
    }
}
