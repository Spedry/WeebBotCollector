package sk.spedry.weebbotcollector.ircbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pircbotx.PircBotX;


public class BotCommands {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final PircBotX bot;

    public BotCommands(PircBotX bot) {
        logger.debug("Creating Bot commands");
        this.bot = bot;
    }

    public void sendMessage(String target, String message) {
        logger.info("Sending message: " + message + " to: " + target);
        bot.sendIRC().message(target, message);
    }
}
