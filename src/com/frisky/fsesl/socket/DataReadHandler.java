package com.frisky.fsesl.socket;

import com.frisky.fsesl.EslConnector;
import com.frisky.fsesl.queues.SocketOutboundMessagesQueue;
import com.frisky.fsesl.threadControl.ThreadControl;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.StandardCharsets;

import static com.frisky.fsesl.constants.EslConstantMessageParts.*;
import static com.frisky.fsesl.constants.EslConstantMessageParts.ERR_INVALID;

public class DataReadHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private long startTime;
    private long endTime;
    private float avgAddTime;
    private long sum = 0;
    private int numberOfAddedMessages = 0;
    private StringBuilder dataSegment = new StringBuilder();
    private int indexOfSingleCR;
    private int indexOfDoubleCR;
    private int contentLength = 0;
    private boolean dataFragmented = false;
    private long numberOfBytes = 0;
    private ThreadControl threadControl;
    int x = 0;

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
        byte[] tempBytes = new byte[dataBytes.readableBytes()];
        dataBytes.readBytes(tempBytes);
        dataSegment.append(new String(tempBytes, StandardCharsets.UTF_8));
        dataSegment.trimToSize();
        //System.out.println("dataSegment: " + dataSegment.toString());
        tempBytes = null;
        if (!threadControl.getIsSocketAuthenticated()) {
            if (dataSegment.indexOf(CONTENT_TYPE) == 0) {
                if (dataSegment.indexOf(AUTH_REQUEST) == (dataSegment.indexOf(CONTENT_TYPE) + CONTENT_TYPE.length())) {
                    System.out.println("Freeswitch asking to authenticate.");
                    System.out.println("Sending Authentication reply.");
                    SocketOutboundMessagesQueue.getQueue().add("auth " + threadControl.getProperty(EslConnector.FS_ESL_PASSWORD) + "\n\n");
                    dataSegment.delete(0, CONTENT_TYPE.length() + AUTH_REQUEST.length());
                } else if (dataSegment.indexOf(COMMAND_REPLY) == CONTENT_TYPE.length()) {
                    if (dataSegment.indexOf(REPLY_TEXT + OK_ACCEPTED) == (CONTENT_TYPE.length() + COMMAND_REPLY.length())) {
                        System.out.println("Socket is authenticated!");
                        SocketOutboundMessagesQueue.getQueue().add("event json " + threadControl.getProperty(EslConnector.FS_ESL_EVENTS) + "\n\n");
                        threadControl.setIsSocketAuthenticated(true);
                        dataSegment.delete(0, CONTENT_TYPE.length() + COMMAND_REPLY.length() + REPLY_TEXT.length() + OK_ACCEPTED.length());

                    }
                } else if (dataSegment.indexOf(REPLY_TEXT + ERR_INVALID) == 0) {
                    dataSegment.delete(0, indexOfDoubleCR);
                }
            }
        } else {
            while (true) {
                indexOfSingleCR = dataSegment.indexOf("\n");
                indexOfDoubleCR = dataSegment.indexOf("\n\n");
                if (indexOfDoubleCR == -1 || indexOfSingleCR == -1)
                    break;

                if (dataSegment.indexOf(CONTENT_LENGTH) == 0) {
                    int numberOfDigits = indexOfSingleCR - CONTENT_LENGTH.length();
                    char[] temp_1 = new char[numberOfDigits];
                    dataSegment.getChars(CONTENT_LENGTH.length(), indexOfSingleCR, temp_1, 0);
                    contentLength = Integer.parseInt(new String(temp_1));
                    if (dataSegment.indexOf(CONTENT_TYPE) == indexOfSingleCR + 1) {
                        if (dataSegment.indexOf(TEXT_EVENT_JSON) == (indexOfSingleCR + 1 + CONTENT_TYPE.length())) {
                            if ((dataSegment.length() - indexOfDoubleCR -2) >= contentLength) {
                                System.out.println("Event:" + dataSegment.substring((indexOfDoubleCR + 2),contentLength));
                                dataSegment.delete(0, indexOfDoubleCR + 2 + contentLength);
                                x++;
                            } else {
                                System.out.println("segmentation");
                                break;
                            }
                        } else {
                            //System.out.println("dataSegment has NO TEXT_EVENT_JSON");
                            //System.out.println("dataSegment content:" + dataSegment.toString());
                        }
                    }
                } else if (dataSegment.indexOf(CONTENT_TYPE) == 0) {
                    if (dataSegment.indexOf(COMMAND_REPLY) == (dataSegment.indexOf(CONTENT_TYPE) + CONTENT_TYPE.length())) {
                        dataSegment.delete(0, indexOfDoubleCR + 2);
                    }
                } else {
                    dataSegment.delete(0, indexOfDoubleCR + 2);
                    break;
                }
            }
        }
        endTime = System.nanoTime();
        sum += (endTime - startTime);
        avgAddTime = sum / numberOfAddedMessages;
        //threadControl.setAverageDataReadTime(avgAddTime);
        //System.out.printf("\r//time:%f//num:%d//", avgAddTime/ 1000000, x);
        if (numberOfAddedMessages == 100000) {
            sum = 0;
            numberOfAddedMessages = 0;
        }
    }
}