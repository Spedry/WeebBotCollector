package sk.spedry.weebbotcollector.properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.spedry.weebbotcollector.util.WCMSetup;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

}
