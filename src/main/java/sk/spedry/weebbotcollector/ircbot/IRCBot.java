package sk.spedry.weebbotcollector.ircbot;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.jmx.Server;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import sk.spedry.weebbotcollector.ircbot.util.BotInfo;
import sk.spedry.weebbotcollector.util.WCMServer;
import sk.spedry.weebbotcollector.util.lists.ServerList;
import sk.spedry.weebbotcollector.work.WBCWorkPlace;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class IRCBot extends ListenerAdapter implements Runnable {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final PircBotX bot;
    private final IRCBotCommands botCommands;
    private final IRCBotListener botListener;
    private WBCWorkPlace workPlace;
    @Getter
    private final String threadName;
    @Getter
    @Setter
    private String userName;
    @Getter
    @Setter
    private String serverName;
    @Getter
    @Setter
    private String channelName;
    @Getter
    @Setter
    private String downloadFolder;

    public IRCBot(BotInfo botInfo, WBCWorkPlace workPlace) {
        this.threadName = botInfo.getServerName() + "_" + botInfo.getChannelName() + "_" + botInfo.getUserName();
        this.userName = botInfo.getUserName();
        this.serverName = botInfo.getServerName();
        this.channelName = botInfo.getChannelName();
        this.downloadFolder = botInfo.getDownloadFolder();
        this.workPlace = workPlace;
        bot = new PircBotX(configureBot());
        botCommands = new IRCBotCommands(bot);
        botListener = new IRCBotListener(downloadFolder, workPlace, botCommands);
    }

    public final Configuration configureBot() {
        logger.debug("Configuring bot");
        return new Configuration.Builder()
                .setName(userName)
                .setAutoNickChange(true)
                //.addServer()
                .addServers()
                //.addAutoJoinChannel(channelName)
                .addAutoJoinChannels()
                .setAutoReconnect(true)
                .addListener(botListener)
                //TODO TEST ON RASPBERRY PI 4 8GB
                .setDccTransferBufferSize(1024*5)
                .setAutoReconnectDelay(() -> 60)
                // this option is set to true by default
                // it means that the bot will safely disconnect
                // from server/channel if thread is shutdown
                .setShutdownHookEnabled(true)
                .buildConfiguration();
    }

    /*private Iterable<Configuration.ServerEntry> getServerList() {
        List<Configuration.ServerEntry> serverList = new ArrayList<Configuration.ServerEntry>();
        for (WCMServer server : workPlace.getServerList(workPlace.getServerListFile()).getServerList()) {
            serverList.add(new Configuration.ServerEntry(server.getServerName()));
        }

        return serverList;
    }*/

    @Override
    public void run() {
        try {
            bot.startBot();
        } catch (IOException e) {
            logger.error("IOException: ", e);
        } catch (IrcException e) {
            logger.error("IrcException", e);
        }
    }
}
