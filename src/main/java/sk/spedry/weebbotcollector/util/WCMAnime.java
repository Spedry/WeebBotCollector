package sk.spedry.weebbotcollector.util;

import lombok.Getter;
import lombok.Setter;

public class WCMAnime {
    @Getter
    @Setter
    private int id;
    @Getter
    private String animeName;
    @Getter
    private String typeOfQuality;
    @Getter
    private String serverName;
    @Getter
    private String numberOfEpisode;
}