package com.frisky.fsesl.constants;

public interface EslConstantMessageParts {

    String CONTENT_TYPE = "Content-Type: ";
    String CONTENT_LENGTH = "Content-Length: ";
    String REPLY_TEXT = "Reply-Text: ";

    String AUTH_REQUEST = "auth/request\n\n";
    String OK_ACCEPTED = "+OK accepted\n\n";
    String OK_EVENT_LISTENER_ENABLED = "+OK event listener enabled json\n\n";
    String ERR_INVALID = "-ERR invalid\n\n";
    String TEXT_EVENT_JSON = "text/event-json\n\n";
    String COMMAND_REPLY = "command/reply\n";
    String DISCONNECT_NOTICE = "text/disconnect-notice\n";
}
