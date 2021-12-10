package sk.spedry.weebbotcollector.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class WCMSetup {
    @Getter
    private String userName;
    @Getter
    private String downloadFolder;
    @Getter
    private String serverName;
    @Getter
    private String channelName;
}
