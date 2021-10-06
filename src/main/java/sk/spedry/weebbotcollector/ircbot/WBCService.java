package sk.spedry.weebbotcollector.ircbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.spedry.weebbotcollector.ircbot.util.BotInfo;
import sk.spedry.weebbotcollector.work.WBCWorkPlace;

public class WBCService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    public void createBot(BotInfo botInfo, WBCWorkPlace workPlace) {
        logger.trace("Creating IRC bot");
        Thread botThread = new Thread(new IRCBot(botInfo, workPlace));
        botThread.setDaemon(true);
        logger.debug("Starting IRC bot thread");
        botThread.start();
    }
}
