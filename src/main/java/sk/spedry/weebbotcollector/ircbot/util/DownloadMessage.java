package sk.spedry.weebbotcollector.ircbot.util;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.checkerframework.common.reflection.qual.NewInstance;

public class DownloadMessage {
    @Getter
    private final String wholeMessage;
    @Getter
    private final String botName;
    @Getter
    private final String message;
    @Getter
    private String animeName;
    @Getter
    @Setter
    private boolean willSetReleaseDate = false;

    public DownloadMessage(@NonNull String message) {
        this.wholeMessage = message;
        String[] spliced = message.split(" ");
        this.botName = spliced[1];
        this.message = spliced[2] + " " + spliced[3] + " " + spliced[4];
    }

    public DownloadMessage(@NonNull String message, @NonNull String animeName) {
        this.wholeMessage = message;
        String[] spliced = message.split(" ");
        this.botName = spliced[1];
        this.message = spliced[2] + " " + spliced[3] + " " + spliced[4];
        this.animeName = animeName;
    }

    public DownloadMessage(@NonNull String botName, @NonNull String message, @NonNull String animeName) {
        this.wholeMessage = "/msg " + botName + " " + message;
        this.botName = botName;
        this.message = message;
        this.animeName = animeName;
    }
}
