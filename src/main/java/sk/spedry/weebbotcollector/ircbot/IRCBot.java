package sk.spedry.weebbotcollector.ircbot;

import lombok.Getter;
import lombok.Setter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;

import sk.spedry.weebbotcollector.util.WCMSetup;
import sk.spedry.weebbotcollector.work.WBCWorkPlace;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class IRCBot extends ListenerAdapter implements Runnable {

    private final Logger logger = LogManager.getLogger(this.getClass());
    @Getter
    private final PircBotX bot;
    private final IRCBotCommands botCommands;
    //TODO WILL BE USED LATER ???
    // private IRCBotListener botListener;
    private final WBCWorkPlace workPlace;
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

    private InetAddress inetAddress;
    public IRCBot(WBCWorkPlace workPlace) {
        /*try {
            inetAddress = InetAddress.getByName("");
        }
        catch (Exception e) {
            logger.error(e);
        }
        fillPortList();*/
        WCMSetup setup =  workPlace.getSetup();
        this.threadName = setup.getServerName() + "_" + setup.getChannelName() + "_" + setup.getUserName();
        this.userName = setup.getUserName();
        this.serverName = setup.getServerName();
        this.channelName = setup.getChannelName();
        this.workPlace = workPlace;
        bot = new PircBotX(configureBot());
        botCommands = new IRCBotCommands(bot);
        workPlace.setBotCommands(botCommands);
        addListener();
    }

    private Configuration configureBot() {
        logger.debug("Configuring bot");
        return new Configuration.Builder()
                .setName(userName)
                .setAutoNickChange(true)
                .addServer(serverName)
                .addAutoJoinChannel(channelName)
                .setAutoReconnect(true)
                // will be added after bot is constructed and added into botCommands
                //.addListener(new IRCBotListener(downloadFolder, workPlace, botCommands))
                // was available in pircbotx 2.2
                // transferred to pircbotx master-snapshot
                // in which was method removed
                //.setDccTransferBufferSize(1024*5)
                //.setDccResumeAcceptTimeout(180)
                //.setDccPublicAddress(inetAddress)
                //.setDccPorts(portList)
                //.setDccPassiveRequest(true)
                .setAutoReconnectDelay(() -> 60)
                .setAutoReconnectAttempts(100)
                // this option is set to true by default
                // it means that the bot will safely disconnect
                // from server/channel if thread is shutdown
                .setShutdownHookEnabled(true)
                .buildConfiguration();
    }

    private void addListener() {
        bot.getConfiguration().getListenerManager().addListener(new IRCBotListener(workPlace, botCommands));
    }

    public void closeBot() {
        if (bot.isConnected()) {
            logger.info("Closing bot");
            bot.close();
        }
    }

    public void resetBot() {
        bot.close();
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        run();
    }

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
