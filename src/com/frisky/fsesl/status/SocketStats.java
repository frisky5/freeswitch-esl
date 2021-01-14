package com.frisky.fsesl.status;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public interface SocketStats {

    AtomicBoolean isSocketConnected = new AtomicBoolean(false);
    AtomicBoolean isSocketAuthenticated = new AtomicBoolean(false);
    AtomicBoolean isWritingThreadRunning = new AtomicBoolean(false);
    AtomicBoolean isWritingThreadValid = new AtomicBoolean(false);
    AtomicBoolean stopWritingThread = new AtomicBoolean(false);
    AtomicBoolean stopSocketThread = new AtomicBoolean(false);
    AtomicBoolean isSocketThreadRunning = new AtomicBoolean(false);
    AtomicLong averageDataReadTime = new AtomicLong(0);


    default boolean getIsSocketConnected() {
        return isSocketConnected.get();
    }

    default void setIsSocketConnected(boolean isConnected) {
        this.isSocketConnected.set(isConnected);
    }

    default boolean getIsSocketAuthenticated() {
        return isSocketAuthenticated.get();
    }

    default void setIsSocketAuthenticated(boolean isAuthenticated) {
        this.isSocketAuthenticated.set(isAuthenticated);
    }

    default boolean getStopSocketThread() {
        return stopSocketThread.get();
    }

    default void setStopSocketThread(boolean stopSocketThread) {
        this.stopSocketThread.set(stopSocketThread);
    }

    default boolean getIsSocketThreadRunning() {
        return isSocketThreadRunning.get();
    }

    default void setIsSocketThreadRunning(boolean isSocketThreadRunning) {
        this.isSocketThreadRunning.set(isSocketThreadRunning);
    }

    default void setAverageDataReadTime(long _averageDataReadTime) {
        averageDataReadTime.set(_averageDataReadTime);
    }

    default long getAverageDataReadTime() {
        return averageDataReadTime.get();
    }

    default boolean getIsWritingThreadRunning() {
        return isWritingThreadRunning.get();
    }

    default void setIsWritingThreadRunning(boolean isRunning) {
        this.isWritingThreadRunning.set(isRunning);
    }

    default boolean getIsWritingThreadValid() {
        return isWritingThreadValid.get();
    }

    default void setIsWritingThreadValid(boolean isValid) {
        this.isWritingThreadValid.set(isValid);
    }

    default boolean getStopWritingThread() {
        return stopWritingThread.get();
    }

    default void setStopWritingThread(boolean stop) {
        this.stopWritingThread.set(stop);
    }
}
