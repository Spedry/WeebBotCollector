package sk.spedry.weebbotcollector.util;

import lombok.Getter;

public class WCMessage {
    @Getter
    private final String messageId;
    @Getter
    private final String messageBody;
    @Getter
    private String additionalData;

    public WCMessage(String messageId, String messageBody) {
        this.messageId = messageId;
        this.messageBody = messageBody;
    }
    public WCMessage(String messageId) {
        this.messageId = messageId;
        this.messageBody = messageId;
    }
}
