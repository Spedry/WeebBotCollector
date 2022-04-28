
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.spedry.weebbotcollector.ircbot.util.DownloadMessage;
import sk.spedry.weebbotcollector.ircbot.util.EpisodeNumber;
import sk.spedry.weebbotcollector.properties.Configuration;
import sk.spedry.weebbotcollector.util.WCMAnime;
import sk.spedry.weebbotcollector.util.lists.AnimeList;
import sk.spedry.weebbotcollector.work.WBCWorkPlace;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

public class EmptyTest {

    private final static Logger logger = LogManager.getLogger(EmptyTest.class);

    public static void main(String[] args) {
        EmptyTest main = new EmptyTest();
        main.logTest();
    }

    public EmptyTest() {

    }

    public void logTest() {
        logger.traceEntry("{} - func - {}", this.getClass().getName(), "LOL");
        logger.traceExit("func - {}", "LOL");
    }

}
