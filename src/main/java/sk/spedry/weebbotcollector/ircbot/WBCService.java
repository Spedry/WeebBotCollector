package sk.spedry.weebbotcollector.ircbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.spedry.weebbotcollector.work.WBCWorkPlace;

public class WBCService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private IRCBot bot;
    private Thread botThread;

    public WBCService() {
        logger.trace("Creating Bot service");
    }

    public void createBotThread(WBCWorkPlace workPlace) {
        if (botThread == null) {
            botThread = new Thread(bot = new IRCBot(workPlace));
            botThread.setName(bot.getClass().getSimpleName());
            botThread.setDaemon(true);
            logger.debug("Setup.json was filled with necessary information, starting bot");
            startBot();
        }
    }

    public void startBot() {
        if (!botThread.isAlive()) {
            logger.debug("Starting IRC bot thread");
            botThread.start();
        }
        else
            logger.error("Bot thread was still alive couldn't create new thread");
    }

    public void closeBot() {
        bot.closeBot();
    }

    public void resetBot() {
        bot.resetBot();
    }
}
