package sk.spedry.weebbotcollector.ircbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.spedry.weebbotcollector.work.WBCWorkPlace;

public class WBCService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private IRCBot bot;
    private Thread botThread;

    public WBCService() {
        logger.traceEntry();
        logger.traceExit();
    }

    public void createBotThread(WBCWorkPlace workPlace) {
        logger.traceEntry();
        if (botThread == null) {
            botThread = new Thread(bot = new IRCBot(workPlace));
            botThread.setName(bot.getClass().getSimpleName());
            botThread.setDaemon(true);
            logger.debug("Setup.json was filled with necessary information, starting bot");
            startBot();
        }
        logger.traceExit();
    }

    public void startBot() {
        logger.traceEntry();
        if (!botThread.isAlive()) {
            logger.debug("Starting IRC bot thread");
            botThread.start();
        }
        else
            logger.error("Bot thread was still alive couldn't create new thread");
        logger.traceExit();
    }

    public void closeBot() {
        logger.traceEntry();
        bot.closeBot();
        logger.traceExit();
    }

    public void resetBot() {
        logger.traceEntry();
        bot.resetBot();
        logger.traceExit();
    }

    public boolean isBotRunning() {
        if (bot.getBot() != null) {
            return bot.getBot().isConnected();
        }
        else {
            logger.error("Bot is null!!!");
            return false;
        }
    }
}
