package sk.spedry.weebbotcollector.properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.spedry.weebbotcollector.util.WCMSetup;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Configuration {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Properties properties;

    public Configuration() {
        this.properties = new Properties();
        try (InputStream inputStream = new FileInputStream(System.getProperty("user.dir")+"/properties/conf.properties")) {
            this.properties.load(inputStream);
        } catch (IOException e) {
            logger.error("Error exception when loading input stream: {}", e.toString());
        }
    }

    public String getProperty(String key) {
        logger.traceEntry(key);
        return logger.traceExit(properties.getProperty(key));
    }

    public boolean getBoolProperty(String key) {
        logger.traceEntry(key);
        if (properties.getProperty(key).equalsIgnoreCase("true"))
            return logger.traceExit(true);
        else
            return logger.traceExit(false);

    }

    public void setProperty(String key, String value) {
        logger.traceEntry("{}={}", key, value);
        properties.setProperty(key, value);
        logger.traceExit();
    }

    /**BOT SETTING**/
    public void setBotSetting(WCMSetup setup) {
        logger.traceEntry();
        setProperty("userName", setup.getUserName());
        setProperty("downloadFolder", setup.getDownloadFolder());
        setProperty("serverName", setup.getUserName());
        setProperty("channelName", setup.getUserName());
        logger.traceExit();
    }

    public WCMSetup getBotSetting() {
        logger.traceEntry();
        return logger.traceExit(new WCMSetup(
                getProperty("userName"),
                getProperty("downloadFolder"),
                getProperty("serverName"),
                getProperty("channelName")
        ));
    }

    /**RELEASE BOTS LIST**/
    public List<String> getAlreadyReleasedBotsList() {
        logger.traceEntry();
        return logger.traceExit(Arrays.asList(getProperty("alreadyReleasedBots").split(",")));
    }

    /**RELEASE BOTS LIST**/
    public List<String> getReleaseBotsList() {
        logger.traceEntry();
        return logger.traceExit(Arrays.asList(getProperty("releaseBots").split(",")));
    }

    /**GET ALL WHO HAS ACCESS**/
    public List<String> getAllWhoHasAccess() {
        //TODO ADD TRACE
        String[] allUsersWhoHasAccess = {"searchBot", "releaseBots", "releaseBotsIPv6", "releaseBotsBatch"};
        List<String> list = new ArrayList<String>();

        for (String user : allUsersWhoHasAccess) {
            list.addAll(Arrays.asList(getProperty(user).split(",")));
        }

        return list;
    }
}
