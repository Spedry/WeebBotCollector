package sk.spedry.weebbotcollector.ircbot;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;


import java.io.IOException;

public class Bot extends ListenerAdapter implements Runnable {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Getter
    private final PircBotX bot;
    private final BotCommands botCommands;

    private final String userName;
    private final String serverName;
    private final String channelName;

    public Bot(String userName, String serverName, String channelName) {
        this.userName = userName;
        this.serverName = serverName;
        this.channelName = channelName;

        logger.debug("Creating bot");
        bot = new PircBotX(configureBot());
        botCommands = new BotCommands(bot);
    }

    public final Configuration configureBot() {
        logger.debug("Configuring bot");
        Configuration config = new Configuration.Builder()
                .setName(userName)
                .setAutoNickChange(true)
                .addServer(serverName)
                .addAutoJoinChannel(channelName)
                .setAutoReconnect(true)
                .addListener(new BotListeners())
                .setDccTransferBufferSize(1024*10)
                .buildConfiguration();
        logger.info("UserName: " + userName + ", ServerName: " + serverName + ", ChannelName :" + channelName);
        return config;
    }

    public void sendMessage(String channelName, String message) {
        botCommands.sendMessage(channelName, message);
    }

    @Override
    public void run() {
        try {
            bot.startBot();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IrcException e) {
            e.printStackTrace();
        }
    }
}
