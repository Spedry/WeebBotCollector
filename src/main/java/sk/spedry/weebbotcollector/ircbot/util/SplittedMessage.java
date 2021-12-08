package sk.spedry.weebbotcollector.ircbot.util;

import lombok.Getter;

public class SplittedMessage {
    @Getter
    private final String message;
    @Getter
    private String info;
    @Getter
    private String size;
    @Getter
    private String animeName;
    @Getter
    private DownloadMessage downloadMessage;

    public SplittedMessage(String message) {
        String[] spliced = message.split("\\*");
        this.message = message;
        this.info = spliced[0].trim();
        this.size = spliced[1].trim();
        this.animeName = spliced[2].trim();
        this.downloadMessage = splitDownloadMessage(spliced[3].trim());
    }

    private DownloadMessage splitDownloadMessage(String message) {
        String[] spliced = message.split(" ");
        return new DownloadMessage(spliced[1], spliced[2] + " " + spliced[3] + " " + spliced[4], animeName);
    }
}
