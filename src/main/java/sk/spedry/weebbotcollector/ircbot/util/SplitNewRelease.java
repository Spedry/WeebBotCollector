package sk.spedry.weebbotcollector.ircbot.util;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SplitNewRelease extends Release {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Getter
    private final String info;
    @Getter
    private final String size;
    @Getter
    private final DownloadMessage downloadMessage;

    public SplitNewRelease(String message) {
        super(message);
        logger.traceEntry();
        String[] spliced = message.split("\\*");
        this.info = spliced[0].trim();
        this.size = spliced[1].trim();
        String animeFileName = spliced[2].trim();
        this.downloadMessage = new DownloadMessage(spliced[3].trim(), animeFileName);
        logger.traceExit(animeFileName);
    }

}
