package sk.spedry.weebbotcollector.ircbot.util;

import lombok.Getter;
import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.spedry.weebbotcollector.util.WCMAnime;
import sk.spedry.weebbotcollector.util.lists.AnimeList;

public class Release {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Getter
    private final String message;
    @Getter
    private String animeFolderName = null;

    public Release(String message) {
        logger.traceEntry("{} - DownloadMessage", this.getClass().getName());
        this.message = message;
        logger.traceExit("DownloadMessage : {}",message);
    }

    public void setAnimeFolderName(@NonNull AnimeList animeList, String animeFileName) {
        logger.traceEntry(animeFileName);
        for (WCMAnime anime : animeList.getAnimeList()) {
            if (animeFileName.contains(anime.getAnimeName().toLowerCase())) {
                animeFolderName = anime.getAnimeName();
            }
        }
        logger.traceExit(animeFolderName);
    }
}