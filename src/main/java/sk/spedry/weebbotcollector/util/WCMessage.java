package sk.spedry.weebbotcollector.util;

import lombok.Getter;

public class WCMessage {
    @Getter
    private String messageId;
    @Getter
    private String messageBody;

    public WCMessage(String messageId, String messageBody) {
        this.messageId = messageId;
        this.messageBody = messageBody;
    }
    public WCMessage(String messageId) {
        this.messageId = messageId;
        this.messageBody = messageId;
    }
}
