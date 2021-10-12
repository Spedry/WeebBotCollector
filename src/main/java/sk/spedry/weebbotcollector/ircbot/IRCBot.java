package sk.spedry.weebbotcollector.ircbot;

import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;
import sk.spedry.weebbotcollector.util.WCMSetup;
import sk.spedry.weebbotcollector.work.WBCWorkPlace;

import java.io.IOException;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class IRCBot extends ListenerAdapter implements Runnable {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final PircBotX bot;
    private final IRCBotCommands botCommands;
    private IRCBotListener botListener;
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

    public IRCBot(WBCWorkPlace workPlace) {
        WCMSetup setup =  workPlace.getSetup();
        this.threadName = setup.getServerName() + "_" + setup.getChannelName() + "_" + setup.getUserName();
        this.userName = setup.getUserName();
        this.serverName = setup.getServerName();
        this.channelName = setup.getChannelName();
        this.downloadFolder = setup.getDownloadFolder();
        this.workPlace = workPlace;
        bot = new PircBotX(configureBot());
        botCommands = new IRCBotCommands(bot);

    }

    private Configuration configureBot() {
        logger.debug("Configuring bot");
        return new Configuration.Builder()
                .setName(userName)
                .setAutoNickChange(true)
                .addServer(serverName)
                .addAutoJoinChannel(channelName)
                .setAutoReconnect(true)
                .addListener(botListener = new IRCBotListener(downloadFolder, workPlace, botCommands))
                //TODO TEST ON RASPBERRY PI 4 8GB
                .setDccTransferBufferSize(1024*5)
                .setAutoReconnectDelay(() -> 60)
                // this option is set to true by default
                // it means that the bot will safely disconnect
                // from server/channel if thread is shutdown
                .setShutdownHookEnabled(true)
                .buildConfiguration();
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

    @Override
    public void onGenericMessage(GenericMessageEvent event) {
        logger.info("{}, Generic message: {}", event.getTimestamp(), event.getMessage());
    }
}
