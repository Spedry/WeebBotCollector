package sk.spedry.weebbotcollector.ircbot.util;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SplitAlreadyReleased extends Release {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Getter
    private final String size;
    @Getter
    private final String animeName;
    @Getter
    private final DownloadMessage downloadMessage;

    public SplitAlreadyReleased(String message) {
        super(message);
        logger.traceEntry();
        this.size = message.substring(message.indexOf("["), message.indexOf("]")+1);
        this.animeName = message.replace(size, "").replace(message.substring(message.lastIndexOf("/msg")), "").trim();
        this.downloadMessage = new DownloadMessage(message.substring(message.lastIndexOf("/msg")), animeName);
        logger.traceExit(animeName);
    }
}
