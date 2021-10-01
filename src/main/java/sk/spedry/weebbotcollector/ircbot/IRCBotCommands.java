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



}
