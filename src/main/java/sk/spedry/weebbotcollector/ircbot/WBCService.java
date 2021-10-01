package sk.spedry.weebbotcollector.ircbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.spedry.weebbotcollector.ircbot.util.BotInfo;

public class WBCService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    public void createBot(BotInfo botInfo) {
        Thread botThread = new Thread(new IRCBot(botInfo));
        botThread.setDaemon(true);
        botThread.start();
    }
}
