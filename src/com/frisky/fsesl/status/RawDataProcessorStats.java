package com.frisky.fsesl.status;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public interface RawDataProcessorStats {
    AtomicBoolean isRawDataProcessorRunning = new AtomicBoolean(false);
    AtomicBoolean stopRawDataProcessor = new AtomicBoolean(false);
    AtomicLong averageEventProcessTime = new AtomicLong(0);

    default boolean getIsRawDataProcessorRunning() {
        return isRawDataProcessorRunning.get();
    }

    default void setIsRawDataProcessorRunning(boolean isRawDataProcessorRunning) {
        this.isRawDataProcessorRunning.set(isRawDataProcessorRunning);
    }

    default boolean getStopRawDataProcessor() {
        return stopRawDataProcessor.get();
    }

    default void setStopRawDataProcessor(boolean  stopRawDataProcessorRunning) {
        this.stopRawDataProcessor.set(stopRawDataProcessorRunning);
    }

    default long getAverageEventProcessTime() {
        return averageEventProcessTime.get();
    }

    default void setAverageEventProcessTime(long averageEventProcessTime) {
        this.averageEventProcessTime.set(averageEventProcessTime);
    }
}
