package sk.spedry.weebbotcollector.util;

import lombok.Getter;
import lombok.Setter;

public class WCMServer {
    @Getter
    @Setter
    private int id;
    @Getter
    private String serverName;
    @Getter
    private String serveChannelName;
}
