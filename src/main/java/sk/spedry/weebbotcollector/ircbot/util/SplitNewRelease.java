package sk.spedry.weebbotcollector.ircbot.util;

import lombok.Getter;
import lombok.NonNull;

import sk.spedry.weebbotcollector.util.WCMAnime;
import sk.spedry.weebbotcollector.util.lists.AnimeList;

public class SplitNewRelease extends Release {
    @Getter
    private final String info;
    @Getter
    private final String size;
    @Getter
    private final String animeName;
    @Getter
    private final DownloadMessage downloadMessage;

    public SplitNewRelease(String message) {
        super(message);
        String[] spliced = message.split("\\*");
        this.info = spliced[0].trim();
        this.size = spliced[1].trim();
        this.animeName = spliced[2].trim();
        this.downloadMessage = new DownloadMessage(spliced[3].trim());
    }

}
