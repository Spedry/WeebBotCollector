package sk.spedry.weebbotcollector.ircbot.util;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;

public class SplitAlreadyReleased extends Release {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Getter
    private final String size;
    @Getter
    private final DownloadMessage downloadMessage;

    public SplitAlreadyReleased(String message) {
        super(message);
        logger.traceEntry();
        this.size = message.substring(message.indexOf("["), message.indexOf("]")+1);
        final String substring = message.toUpperCase(Locale.ROOT).substring(message.toUpperCase(Locale.ROOT).lastIndexOf("/MSG"));
        String animeFileName = message.replace(size, "").replace(substring, "").trim();
        this.downloadMessage = new DownloadMessage(substring, animeFileName);
        logger.traceExit(animeFileName);
    }
}
