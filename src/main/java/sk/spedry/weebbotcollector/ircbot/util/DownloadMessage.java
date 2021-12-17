package sk.spedry.weebbotcollector.ircbot.util;

import lombok.Getter;

public class DownloadMessage {
    @Getter
    private final String wholeMessage;
    @Getter
    private final String botName;
    @Getter
    private final String message;
    @Getter
    private String animeName;

    public DownloadMessage(String message) {
        this.wholeMessage = message;
        String[] spliced = message.split(" ");
        this.botName = spliced[1];
        this.message = spliced[2] + " " + spliced[3] + " " + spliced[4];
    }

    public DownloadMessage(String botName, String message, String animeName) {
        this.wholeMessage = "/msg " + botName + " " + message;
        this.botName = botName;
        this.message = message;
        this.animeName = animeName;
    }
}
