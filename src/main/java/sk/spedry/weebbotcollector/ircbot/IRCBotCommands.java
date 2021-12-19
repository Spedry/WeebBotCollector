package sk.spedry.weebbotcollector.ircbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pircbotx.PircBotX;
import sk.spedry.weebbotcollector.ircbot.util.DownloadMessage;

import java.util.Objects;

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

    public void sendMessage(DownloadMessage message) {
        logger.info("Sending message: " + message.getMessage() + " to: " + message.getBotName());
        bot.sendIRC().message(message.getBotName(), message.getMessage());
    }

    public void searchAnime(String target, String animeName, String numberOfEp, String quality) {
        String searchFor = "!s " + animeName + " " + numberOfEp + " " + quality;
        sendMessage(target, searchFor);
    }

    public void searchAnime(String target, String downloadFrom, String animeName, String numberOfEp, String quality) {
        if (Objects.equals(downloadFrom, "")) {
            searchAnime(target, animeName, numberOfEp, quality);
            return;
        }
        String searchFor = "!s " + downloadFrom + " " + animeName + " " + numberOfEp + " " + quality;
        sendMessage(target, searchFor);
    }
}
