package sk.spedry.weebbotcollector.ircbot.util;

import lombok.Getter;

public class SplitAlreadyReleased {
    @Getter
    private final String message;
    @Getter
    private final String size;
    @Getter
    private final String animeName;
    @Getter
    private final DownloadMessage downloadMessage;

    public SplitAlreadyReleased(String message) {
        this.message = message;
        this.size = message.substring(message.indexOf("["), message.indexOf("]")+1);
        this.animeName = message.replace(size, "").replace(message.substring(message.lastIndexOf("/msg")), "").trim();
        this.downloadMessage = new DownloadMessage(message.substring(message.lastIndexOf("/msg")), animeName);
    }
}
