package com.frisky.fsesl.processors;

import com.frisky.fsesl.EslConnector;
import com.frisky.fsesl.queues.SocketOutboundMessagesQueue;
import com.frisky.fsesl.queues.SocketProcessedDataQueue;
import com.frisky.fsesl.queues.SocketRawDataQueue;
import com.frisky.fsesl.threadControl.ThreadControl;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.frisky.fsesl.constants.EslConstantMessageParts.*;

public class RawDataProcessor implements Runnable {
    private StringBuilder dataSegment = new StringBuilder();
    private LinkedBlockingQueue<String> socketRawDataQueue = SocketRawDataQueue.getQueue();
    private ThreadControl threadControl;
    private boolean authSent = false;
    private int indexOfSingleCR;
    private int indexOfDoubleCR;

    public RawDataProcessor(ThreadControl _threadControl) {
        this.threadControl = _threadControl;
    }

    @Override
    public void run() {
        threadControl.setIsRawDataProcessorRunning(true);
        while (!threadControl.getStopRawDataProcessor()) {
            if (!threadControl.getIsSocketAuthenticated()) {
                if (PollDataSegment()) continue;
                if (dataSegment.indexOf(CONTENT_TYPE) == 0) {
                    dataSegment.delete(dataSegment.indexOf(CONTENT_TYPE), CONTENT_TYPE.length());
                    if (dataSegment.indexOf(AUTH_REQUEST) == 0) {
                        dataSegment.delete(dataSegment.indexOf(AUTH_REQUEST), AUTH_REQUEST.length());
                        System.out.println("Freeswitch asking to authenticate, authenticating!");
                        SocketOutboundMessagesQueue.getQueue().add("auth " + threadControl.getProperty(EslConnector.FS_ESL_PASSWORD) + "\n\n");
                        authSent = true;
                    } else if (dataSegment.indexOf(COMMAND_REPLY) == 0 && authSent) {
                        dataSegment.delete(dataSegment.indexOf(COMMAND_REPLY), COMMAND_REPLY.length());
                        if (dataSegment.indexOf(REPLY_TEXT + OK_ACCEPTED) == 0) {
                            dataSegment.delete(dataSegment.indexOf(REPLY_TEXT + OK_ACCEPTED), (REPLY_TEXT + OK_ACCEPTED).length());
                            System.out.println("Socket is authenticated! : " + dataSegment.length());
                            SocketOutboundMessagesQueue.getQueue().add("event json " + threadControl.getProperty(EslConnector.FS_ESL_EVENTS) + "\n\n");
                            threadControl.setIsSocketAuthenticated(true);
                        }
                    } else if (dataSegment.indexOf(REPLY_TEXT + ERR_INVALID) == 0 && authSent) {
                        dataSegment.delete(dataSegment.indexOf(REPLY_TEXT + ERR_INVALID), (REPLY_TEXT + ERR_INVALID).length());
                        System.out.println("failed to authenticate socket!");
                    }
                }
            } else {
                boolean fragmentedData = false;
                int contentLength = 0;
                while (!threadControl.getStopRawDataProcessor()) {
                    if (PollDataSegment()) {
                        continue;
                    }
                    System.out.println("length: " + dataSegment.length() + " // capacity : " + dataSegment.capacity());
                    while (true) {
                        indexOfSingleCR = dataSegment.indexOf("\n");
                        indexOfDoubleCR = dataSegment.indexOf("\n\n");
                        if (dataSegment.indexOf(CONTENT_LENGTH) == 0) {
                            if (indexOfDoubleCR == -1) {
                                break;
                            }
                            contentLength = Integer.parseInt(dataSegment.substring(CONTENT_LENGTH.length(), indexOfSingleCR));
                            if (dataSegment.indexOf(CONTENT_TYPE) == indexOfSingleCR + 1) {
                                if (dataSegment.indexOf(TEXT_EVENT_JSON) == (indexOfSingleCR + 1 + CONTENT_TYPE.length())) {
                                    if (dataSegment.substring(indexOfDoubleCR + 2, dataSegment.length()).length() >= contentLength) {
                                        dataSegment.delete(0, indexOfDoubleCR + 2);
                                        SocketProcessedDataQueue.getQueue().add(dataSegment.substring(0, contentLength));
                                        SocketProcessedDataQueue.getQueue().remove();
                                        dataSegment.delete(0, contentLength);
                                        fragmentedData = false;
                                    } else if (dataSegment.length() < contentLength) {
                                        fragmentedData = true;
                                        break;
                                    }
                                }
                            }
                        } else if (dataSegment.indexOf(CONTENT_TYPE) == 0) {
                            if (dataSegment.indexOf(COMMAND_REPLY) == (dataSegment.indexOf(CONTENT_TYPE) + CONTENT_TYPE.length())) {
                                if (dataSegment.indexOf(REPLY_TEXT) == (dataSegment.indexOf(CONTENT_TYPE) + CONTENT_TYPE.length() + COMMAND_REPLY.length())) {
                                    dataSegment.delete(0, indexOfDoubleCR + 2);
                                }
                            }
                        } else {
                            if (indexOfDoubleCR != -1) {
                                dataSegment.delete(0, indexOfDoubleCR + 2);
                            }
                            break;
                        }
                    }

                }
            }
            threadControl.setIsRawDataProcessorRunning(false);
        }
    }

    private boolean PollDataSegment() {
        try {
            String temp = socketRawDataQueue.poll(1, TimeUnit.SECONDS);
            if (!Objects.isNull(temp)) {
                dataSegment.append(temp);
                temp = null;
                return false;
            } else {
                return true;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return true;
        }
    }
}
