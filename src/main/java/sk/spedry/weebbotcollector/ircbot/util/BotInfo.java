package sk.spedry.weebbotcollector.ircbot.util;

import lombok.Getter;

public class BotInfo {
    @Getter
    private String userName;
    @Getter
    private String serverName;
    @Getter
    private String channelName;
    @Getter
    private String downloadFolder;
    // TODO DELETE
    public BotInfo(String userName, String serverName, String channelName, String downloadFolder) {
        this.userName = userName;
        this.serverName = serverName;
        this.channelName = channelName;
        this.downloadFolder = downloadFolder;
    }
}
