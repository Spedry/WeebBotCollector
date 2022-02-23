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
    private CodeTable typeOfQuality;
    @Getter
    private String botName;
    @Getter
    private int numberOfEpisodes;
    @Getter
    private int numberOfDownloadedEpisodes;
    @Getter
    @Setter
    private boolean wasDownloaded;

    public void increaseNumberOfDownloadedEpisodes() {
        numberOfDownloadedEpisodes++;
    }
}
