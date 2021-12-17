package sk.spedry.weebbotcollector.ircbot.util;

import lombok.Getter;
import lombok.NonNull;

import sk.spedry.weebbotcollector.util.WCMAnime;
import sk.spedry.weebbotcollector.util.lists.AnimeList;

public class SplitNewRelease {
    @Getter
    private final String message;
    @Getter
    private final String info;
    @Getter
    private final String size;
    @Getter
    private final String animeName;
    @Getter
    private String animeFolderName = null;
    @Getter
    private final DownloadMessage downloadMessage;

    public SplitNewRelease(String message) {
        String[] spliced = message.split("\\*");
        this.message = message;
        this.info = spliced[0].trim();
        this.size = spliced[1].trim();
        this.animeName = spliced[2].trim();
        this.downloadMessage = new DownloadMessage(spliced[3].trim());
    }

    public void setAnimeFolderName(@NonNull AnimeList animeList) {
        for (WCMAnime anime : animeList.getAnimeList()) {
            if (animeName.contains(anime.getAnimeName().toLowerCase())) {
                animeFolderName = anime.getAnimeName();
            }
        }
    }
}
