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
}
