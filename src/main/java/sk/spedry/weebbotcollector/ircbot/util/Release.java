package sk.spedry.weebbotcollector.ircbot.util;

import lombok.Getter;
import lombok.NonNull;
import sk.spedry.weebbotcollector.util.WCMAnime;
import sk.spedry.weebbotcollector.util.lists.AnimeList;

public class Release {
    @Getter
    private final String message;
    @Getter
    private String animeFolderName = null;

    public Release(String message) {
        this.message = message;
    }

    public void setAnimeFolderName(@NonNull AnimeList animeList, String animeName) {
        for (WCMAnime anime : animeList.getAnimeList()) {
            if (animeName.contains(anime.getAnimeName().toLowerCase())) {
                animeFolderName = anime.getAnimeName();
            }
        }
    }
}
