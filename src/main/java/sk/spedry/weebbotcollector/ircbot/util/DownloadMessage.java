package sk.spedry.weebbotcollector.ircbot.util;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DownloadMessage {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Getter
    private final String wholeMessage;
    @Getter
    private final String botName;
    @Getter
    private final String message;
    @Getter
    private String animeFileName;
    @Getter
    private String animeName;
    @Getter
    private String animeQuality;
    @Getter
    private EpisodeNumber episodeNumber;
    @Getter
    @Setter
    private boolean willSetReleaseDate = false;

    public DownloadMessage(@NonNull String message) {
        logger.traceEntry("DownloadMessage");
        this.wholeMessage = message;
        String[] spliced = message.split(" ");
        this.botName = spliced[1];
        this.message = spliced[2] + " " + spliced[3] + " " + spliced[4];
        logger.traceExit("DownloadMessage - {}", message);
    }

    public DownloadMessage(@NonNull String message, @NonNull String animeFileName) {
        logger.traceEntry("DownloadMessage");
        this.wholeMessage = message;
        String[] spliced = message.split(" ");
        this.botName = spliced[1];
        this.message = spliced[2] + " " + spliced[3] + " " + spliced[4];
        this.animeFileName = animeFileName;
        setEpisodeNumber();
        setAnimeName();
        setAnimeQuality();
        logger.traceExit("DownloadMessage - {}", animeFileName);
    }

    public DownloadMessage(@NonNull String botName, @NonNull String message, @NonNull String animeFileName) {
        logger.traceEntry("DownloadMessage");
        this.wholeMessage = "/msg " + botName + " " + message;
        this.botName = botName;
        this.message = message;
        this.animeFileName = animeFileName;
        setEpisodeNumber();
        setAnimeName();
        setAnimeQuality();
        logger.traceExit("DownloadMessage - {}", animeFileName);
    }

    private void setEpisodeNumber() {
        logger.traceEntry();
        String[] spliced = animeFileName.split(" ");
        episodeNumber = new EpisodeNumber(spliced[spliced.length-3]);
        logger.traceExit(episodeNumber);
    }

    public void setAnimeName() {
        logger.traceEntry();
        String[] spliced = animeFileName.split(" ");
        String animeName = spliced[0];
        for (int i = 1; i < spliced.length-4; i++) {
            animeName = animeName + " " + spliced[i];
        }
        animeName = animeName + " - " + episodeNumber.getEpisodeNumberString();
        animeName = animeName.substring(animeName.indexOf("[subsplease]"));
        System.out.println(animeName);
        this.animeName = animeName;
        logger.traceExit(animeName);
    }

    public void setAnimeQuality() {
        logger.traceEntry();
        String[] spliced = animeFileName.split(" ");
        String animeQuality = spliced[spliced.length-2];
        animeQuality = animeQuality.replace("(", "").replace(")", "");
        this.animeQuality = animeQuality;
        logger.traceExit(animeQuality);
    }
}
