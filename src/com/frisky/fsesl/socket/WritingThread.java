package com.frisky.fsesl.socket;

import com.frisky.fsesl.queues.SocketOutboundMessagesQueue;
import com.frisky.fsesl.threadControl.ThreadControl;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.util.CharsetUtil;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class WritingThread implements Runnable {
    private Channel channel = null;
    private LinkedBlockingQueue<String> socketOutboundMessagesQueue = SocketOutboundMessagesQueue.getQueue();
    private ThreadControl threadControl;
    private AtomicBoolean isRunning = new AtomicBoolean(true);
    private AtomicBoolean isStopped = new AtomicBoolean(false);
    private AtomicBoolean stop = new AtomicBoolean(false);

    private String message;

    public WritingThread(Channel _channel, ThreadControl _threadControl) {
        this.threadControl = _threadControl;
        if (Objects.isNull(_channel)) {
            threadControl.setIsWritingThreadValid(false);

            return;
        }
        threadControl.setIsWritingThreadValid(true);

        this.channel = _channel;
    }

    @Override
    public void run() {
        if (threadControl.getIsWritingThreadValid()) {
            threadControl.setIsWritingThreadRunning(true);
            while (!threadControl.getStopWritingThread()) {
                try {
                    message = socketOutboundMessagesQueue.take();
                    //System.out.println("writing message: " + message);
                    channel.writeAndFlush(Unpooled.copiedBuffer(message, CharsetUtil.UTF_8));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        threadControl.setIsWritingThreadRunning(false);
    }
}
