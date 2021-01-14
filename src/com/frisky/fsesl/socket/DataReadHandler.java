package com.frisky.fsesl.socket;

import com.frisky.fsesl.processors.RawDataProcessor;
import com.frisky.fsesl.queues.SocketRawDataQueue;
import com.frisky.fsesl.threadControl.ThreadControl;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import java.math.BigInteger;
import java.util.concurrent.LinkedBlockingQueue;

public class DataReadHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private long startTime;
    private long endTime;
    private long avgAddTime;
    private long sum = 0;
    private int numberOfAddedMessages = 0;
    private LinkedBlockingQueue<String> socketRawDataQueue = SocketRawDataQueue.getQueue();
    private StringBuilder stringBuilder_1 = new StringBuilder();

    private int contentLength = 0;
    private boolean dataFragmented = false;

    private ThreadControl threadControl;

    public DataReadHandler(ThreadControl _threadControl) {
        this.threadControl = _threadControl;
    }

    @Override
    public void channelActive(ChannelHandlerContext channelHandlerContext) {
        threadControl.setIsSocketConnected(true);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) {
        cause.printStackTrace();
        channelHandlerContext.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf dataBytes) {
        startTime = System.nanoTime();
        numberOfAddedMessages++;
        socketRawDataQueue.add(dataBytes.toString(CharsetUtil.UTF_8));
        endTime = System.nanoTime();
        sum += (endTime - startTime);
        avgAddTime = sum / numberOfAddedMessages;
        threadControl.setAverageDataReadTime(avgAddTime);
        //System.out.println("avg adding time: " + avgAddTime + " ns");
        if (numberOfAddedMessages == 3000) {
            sum = 0;
            numberOfAddedMessages = 0;
        }

    }
}