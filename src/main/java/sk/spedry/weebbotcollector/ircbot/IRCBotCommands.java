package sk.spedry.weebbotcollector.ircbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pircbotx.PircBotX;

public class IRCBotCommands {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final PircBotX bot;

    public IRCBotCommands(PircBotX bot) {
        this.bot = bot;
    }

    public void sendMessage(String target, String message) {
        logger.info("Sending message: " + message + " to: " + target);
        bot.sendIRC().message(target, message);
    }

    public String[] splitDownloadMessage(String downloadMessage) {
        logger.info("Splitting this msg: {}", downloadMessage);
        String[] spliced = downloadMessage.split(" ");
        String botName = spliced[1];
        String message = spliced[2] + " " + spliced[3] + " " + spliced[4];
        return new String[] {botName, message};
    }

}
