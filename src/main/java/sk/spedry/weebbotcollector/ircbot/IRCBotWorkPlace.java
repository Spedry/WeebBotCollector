package sk.spedry.weebbotcollector.ircbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.spedry.weebbotcollector.ircbot.util.SplitAlreadyReleased;
import sk.spedry.weebbotcollector.ircbot.util.SplitNewRelease;
import sk.spedry.weebbotcollector.work.WBCWorkPlace;

import java.io.File;

public class IRCBotWorkPlace {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final WBCWorkPlace workPlace;

    public IRCBotWorkPlace(WBCWorkPlace workPlace) {
        logger.traceEntry();
        this.workPlace = workPlace;
        logger.traceExit();
    }

    public String bytesToReadable(long bytes) {
        logger.traceEntry();
        double size_kb = bytes / 1024;
        double size_mb = size_kb / 1024;
        double size_gb = size_mb / 1024 ;

        if (size_gb > 0){
            return logger.traceExit(String.format("%.3f", size_gb) + " GB");
        } else if(size_mb > 0) {
            return logger.traceExit(String.format("%.3f", size_gb) + " MB");
        } else {
            return logger.traceExit(String.format("%.3f", size_gb) + " KB");
        }
    }
}
