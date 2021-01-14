package com.frisky.fsesl.threadControl;

import com.frisky.fsesl.status.RawDataProcessorStats;
import com.frisky.fsesl.status.SocketStats;

import java.util.Properties;

public class ThreadControl implements SocketStats, RawDataProcessorStats {
    private final Properties properties;

    public ThreadControl(Properties _properties) {
        properties = _properties;
    }

    public String getProperty(String _property) {
        return properties.getProperty(_property);
    }
}
