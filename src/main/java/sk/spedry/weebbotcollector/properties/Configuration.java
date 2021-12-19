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
        return properties.getProperty(key);
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /**BOT SETTING**/
    public void setBotSetting(WCMSetup setup) {
        setProperty("userName", setup.getUserName());
        setProperty("downloadFolder", setup.getDownloadFolder());
        setProperty("serverName", setup.getUserName());
        setProperty("channelName", setup.getUserName());
    }

    public WCMSetup getBotSetting() {
        return new WCMSetup(
                getProperty("userName"),
                getProperty("downloadFolder"),
                getProperty("serverName"),
                getProperty("channelName")
        );
    }

    /**RELEASE BOTS LIST**/
    public List<String> getReleaseBotsList() {
        return Arrays.asList(getProperty("releaseBots").split(","));
    }

    /**GET ALL WHO HAS ACCESS**/
    public List<String> getAllWhoHasAccess() {
        String[] allUsersWhoHasAccess = {"searchBot", "releaseBots", "releaseBotsIPv6", "releaseBotsBatch"};
        List<String> list = new ArrayList<String>();

        for (String user : allUsersWhoHasAccess) {
            list.addAll(Arrays.asList(getProperty(user).split(",")));
        }

        return list;
    }
}
