package sk.spedry.weebbotcollector.ircbot;

import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pircbotx.PircBotX;
import sk.spedry.weebbotcollector.ircbot.util.DownloadMessage;

import java.util.ArrayList;
import java.util.Objects;

public class IRCBotCommands {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final PircBotX bot;
    @Setter
    private IRCBotListener botListener;

    public IRCBotCommands(PircBotX bot) {
        this.bot = bot;
    }

    public void sendMessage(String target, String message) {
        logger.traceEntry("To: {} : {}", target, message);
        logger.info("Sending message: " + message + " to: " + target);
        bot.sendIRC().message(target, message);
        logger.traceExit();
    }

    public void sendMessage(DownloadMessage message) {
        logger.traceExit(message);
        logger.info("Sending message: {} to: {}, to download: {}", message.getMessage(), message.getBotName(), message.getAnimeFileName());
        bot.sendIRC().message(message.getBotName(), message.getMessage());
        logger.traceExit();
    }

    public void searchAnime(String target, String animeName, String numberOfEp, String quality) {
        logger.traceEntry("To: {}", target);
        String searchFor = "!s " + animeName + " " + numberOfEp + " " + quality;
        sendMessage(target, searchFor);
        logger.traceExit(searchFor);
    }

    public void searchAnime(String target, String downloadFrom, String animeName, String numberOfEp, String quality) {
        logger.traceEntry("To: {}", target);
        if (Objects.equals(downloadFrom, "")) {
            searchAnime(target, animeName, numberOfEp, quality);
            return;
        }
        String searchFor = "!s " + downloadFrom + " " + animeName + " " + numberOfEp + " " + quality;
        sendMessage(target, searchFor);
        logger.traceExit(searchFor);
    }

    public ArrayList<DownloadMessage> getDownloadQueueList() {
        return botListener.getDownloadQueue();
    }

    public ArrayList<DownloadMessage> getAlreadyReleasedQueueList() {
        return botListener.getAlreadyReleasedQueue();
    }

    public DownloadMessage getCurrentlyDownloadingAnime() {
        return botListener.getCurrentlyDownloading();
    }
}
