package com.frisky.fsesl;

import com.frisky.fsesl.socket.SocketThread;
import com.frisky.fsesl.threadControl.ThreadControl;

import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class EslConnector {
    private ThreadControl threadControl;
    private boolean isValid = false;

    public static String FS_ESL_IP_ADDRESS = "fsEslIpAddress";
    public static String FS_ESL_PASSWORD = "fsEslPassword";
    public static String FS_ESL_PORT = "fsEslPort";
    public static String FS_ESL_EVENTS = "fsEslEventS";

    public EslConnector(Properties _properties) {
        if (Objects.isNull(_properties)) {
            System.out.println("Properties passed object is null!");
            return;
        }
        if (Objects.isNull(_properties.getProperty(FS_ESL_IP_ADDRESS))) {
            System.out.println("FS ESL IP Address is missing!");
            return;
        }
        if (Objects.isNull(_properties.getProperty(FS_ESL_PASSWORD))) {
            System.out.println("FS ESL Password is missing!");
            return;
        }
        if (Objects.isNull(_properties.getProperty(FS_ESL_PORT))) {
            System.out.println("FS ESL Port is missing!");
            return;
        }
        if (Objects.isNull(_properties.getProperty(FS_ESL_EVENTS))) {
            System.out.println("FS ESL Events are missing, but will default to all events! this is not an issue");
            _properties.setProperty(FS_ESL_EVENTS, "all");
        }

        final String zeroTo255 = "(\\d{1,2}|(0|1)\\d{2}|2[0-4]\\d|25[0-5])";
        final String regex = zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(_properties.getProperty(FS_ESL_IP_ADDRESS));
        if (!matcher.matches()) {
            System.out.println("invalid IP address");
            return;
        }
        try {
            Integer.parseInt(_properties.getProperty(FS_ESL_PORT));
        } catch (Exception e) {
            System.out.println("invalid Port number");
            return;
        }
        if (Integer.parseInt(_properties.getProperty(FS_ESL_PORT)) < 0 || Integer.parseInt(_properties.getProperty(FS_ESL_PORT)) > 65353) {
            System.out.println("invalid Port number");
            return;
        }
        isValid = true;
        threadControl = new ThreadControl(_properties);
    }

    public void connect() {
        if (!isValid) {
            System.out.println("Provided properties are not valid, cannot connect. check logs to determine what property is wrong or missing!");
            return;
        }
        SocketThread socketThread = new SocketThread(threadControl);
        Thread thread = new Thread(socketThread);
        thread.start();
    }
}
