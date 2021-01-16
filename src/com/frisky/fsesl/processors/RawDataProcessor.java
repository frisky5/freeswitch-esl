package com.frisky.fsesl.processors;

import com.frisky.fsesl.EslConnector;
import com.frisky.fsesl.queues.SocketOutboundMessagesQueue;
import com.frisky.fsesl.queues.SocketProcessedDataQueue;
import com.frisky.fsesl.queues.SocketRawDataQueue;
import com.frisky.fsesl.threadControl.ThreadControl;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.frisky.fsesl.constants.EslConstantMessageParts.*;

public class RawDataProcessor implements Runnable {

    private LinkedBlockingQueue<byte[]> socketRawDataQueue = SocketRawDataQueue.getQueue();
    private ThreadControl threadControl;
    private StringBuilder dataSegment = new StringBuilder();

    public RawDataProcessor(ThreadControl _threadControl) {
        this.threadControl = _threadControl;
    }

    @Override
    public void run() {
        boolean authSent = false;
        int indexOfSingleCR;
        int indexOfDoubleCR;

        threadControl.setIsRawDataProcessorRunning(true);
        while (!threadControl.getStopRawDataProcessor()) {
            if (!threadControl.getIsSocketAuthenticated()) {
                if (!pollData()) {
                    continue;
                }
                if (dataSegment.indexOf(CONTENT_TYPE) == 0) {
                    if (dataSegment.indexOf(AUTH_REQUEST) == (dataSegment.indexOf(CONTENT_TYPE) + CONTENT_TYPE.length())) {
                        System.out.println("Freeswitch asking to authenticate, authenticating!");
                        SocketOutboundMessagesQueue.getQueue().add("auth " + threadControl.getProperty(EslConnector.FS_ESL_PASSWORD) + "\n\n");
                        authSent = true;
                        dataSegment = new StringBuilder(dataSegment.substring((CONTENT_TYPE.length() + AUTH_REQUEST.length())));

                    } else if (dataSegment.indexOf(COMMAND_REPLY) == CONTENT_TYPE.length() && authSent) {
                        if (dataSegment.indexOf(REPLY_TEXT + OK_ACCEPTED) == (dataSegment.indexOf(COMMAND_REPLY) + COMMAND_REPLY.length())) {
                            dataSegment = new StringBuilder(dataSegment.substring(CONTENT_TYPE.length() + COMMAND_REPLY.length() + REPLY_TEXT.length() + OK_ACCEPTED.length()));
                            System.out.println("Socket is authenticated!");
                            SocketOutboundMessagesQueue.getQueue().add("event json " + threadControl.getProperty(EslConnector.FS_ESL_EVENTS) + "\n\n");
                            threadControl.setIsSocketAuthenticated(true);
                        }
                    } else if (dataSegment.indexOf(REPLY_TEXT + ERR_INVALID) == 0 && authSent) {
                        dataSegment = new StringBuilder(dataSegment.substring(dataSegment.indexOf("\n\n")));
                        System.out.println("failed to authenticate socket!");
                    }
                }
            } else {
                boolean fragmentedData = false;
                int contentLength;

                while (!threadControl.getStopRawDataProcessor()) {
                    if (!pollData()) {
                        continue;
                    }
                    while (true) {
                        indexOfSingleCR = dataSegment.indexOf("\n");
                        indexOfDoubleCR = dataSegment.indexOf("\n\n");
                        if (dataSegment.indexOf(CONTENT_LENGTH) == 0) {
                            if (indexOfDoubleCR == -1) {
                                break;
                            }
                            int numberOfDigits = indexOfSingleCR - CONTENT_LENGTH.length();
                            char[] temp_1 = new char[numberOfDigits];
                            dataSegment.getChars(CONTENT_LENGTH.length(), indexOfSingleCR, temp_1, 0);
                            String temp_2 = new String(temp_1);
                            contentLength = Integer.parseInt(temp_2);
                            if (dataSegment.indexOf(CONTENT_TYPE) == indexOfSingleCR + 1) {
                                if (dataSegment.indexOf(TEXT_EVENT_JSON) == (indexOfSingleCR + 1 + CONTENT_TYPE.length())) {
                                    if ((dataSegment.length() - indexOfDoubleCR) >= contentLength) {
                                        SocketProcessedDataQueue.getQueue().add(dataSegment.substring(indexOfDoubleCR + 2, contentLength));
                                        SocketProcessedDataQueue.getQueue().remove();
                                        dataSegment.delete(0, indexOfDoubleCR + 2 + contentLength);
                                        //dataSegment.trimToSize();
                                        //System.out.println("event!");
                                        fragmentedData = dataSegment.length() > 0;
                                    } else if (dataSegment.length() < contentLength) {
                                        fragmentedData = true;
                                        break;
                                    }
                                }
                            }
                        } else if (dataSegment.indexOf(CONTENT_TYPE) == 0) {
                            if (dataSegment.indexOf(COMMAND_REPLY) == (dataSegment.indexOf(CONTENT_TYPE) + CONTENT_TYPE.length())) {
                                System.out.println("command");
                                dataSegment.delete(0, indexOfDoubleCR + 2);
                                dataSegment.trimToSize();
                            }
                        } else {
                            if (indexOfDoubleCR != -1) {
                                System.out.println("weird data: " + dataSegment.toString());
                                dataSegment = new StringBuilder(dataSegment.substring(indexOfDoubleCR + 2));
                                return;
                            }
                            break;
                        }
                    }
                }
            }
            threadControl.setIsRawDataProcessorRunning(false);
        }
    }

    private boolean pollData() {
        try {
            dataSegment.append(Charset.forName("UTF-8").decode(ByteBuffer.wrap(socketRawDataQueue.take())));
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}
