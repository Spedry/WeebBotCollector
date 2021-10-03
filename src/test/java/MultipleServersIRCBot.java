import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import sk.spedry.weebbotcollector.ircbot.IRCBotCommands;
import sk.spedry.weebbotcollector.ircbot.IRCBotListener;
import sk.spedry.weebbotcollector.work.WBCWorkPlace;

import java.io.IOException;

public class MultipleServersIRCBot implements Runnable {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final PircBotX bot;
    private final IRCBotCommands botCommands;
    private final IRCBotListener botListener;

    private final String userDir = System.getProperty("user.dir");

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

    public MultipleServersIRCBot() {
        this.threadName = "Test thread.";
        this.userName = "Spedry";
        this.serverName = "irc.rizon.net";
        this.channelName = "#NIBL";
        this.downloadFolder = userDir + "tempDownloads";
        bot = new PircBotX(configureBot());
        botCommands = new IRCBotCommands(bot);
        botListener = new IRCBotListener(downloadFolder, null, botCommands);
    }

    public final Configuration configureBot() {
        logger.debug("Configuring bot");
        return new Configuration.Builder()
                .setName(userName)
                .setAutoNickChange(true)
                .addServer(serverName)
                .addAutoJoinChannel(channelName)
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

    public static void main(String[] args) {
        MultipleServersIRCBot ircBot = new MultipleServersIRCBot();
        new Thread(ircBot).start();
    }
}
