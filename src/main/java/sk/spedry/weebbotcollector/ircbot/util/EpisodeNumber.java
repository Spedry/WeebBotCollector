package sk.spedry.weebbotcollector.ircbot.util;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EpisodeNumber {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Getter
    private final int episodeNumber;
    @Getter
    private final String episodeNumberString;
    @Getter
    private final String episodeVersion;
    @Getter
    private final int episodeCode;

    public EpisodeNumber(String episodeNV) {
        logger.traceEntry(episodeNV);
        if (episodeNV.length() >= 4) {
            episodeNumberString = episodeNV.substring(0, episodeNV.length() - 2);
            episodeNumber = Integer.parseInt(episodeNumberString);
            episodeVersion = episodeNV.substring(episodeNV.length() - 2);
            episodeCode = Integer.parseInt(episodeVersion.substring(episodeVersion.length() - 1));
        }
        else {
            episodeNumberString = episodeNV;
            episodeNumber = Integer.parseInt(episodeNV);
            episodeVersion = "v1";
            episodeCode = 1;
        }
        logger.traceExit(episodeVersion);
    }
}
