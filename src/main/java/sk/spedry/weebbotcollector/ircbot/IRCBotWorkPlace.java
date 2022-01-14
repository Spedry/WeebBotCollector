package sk.spedry.weebbotcollector.ircbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.spedry.weebbotcollector.ircbot.util.SplitAlreadyReleased;
import sk.spedry.weebbotcollector.ircbot.util.SplitNewRelease;
import sk.spedry.weebbotcollector.work.WBCWorkPlace;

import java.io.File;

public class IRCBotWorkPlace {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final WBCWorkPlace workPlace;

    public IRCBotWorkPlace(WBCWorkPlace workPlace) {
        this.workPlace = workPlace;
    }

    public String bytesToReadable(long bytes) {
        double size_kb = bytes / 1024;
        double size_mb = size_kb / 1024;
        double size_gb = size_mb / 1024 ;

        if (size_gb > 0){
            return String.format("%.3f", size_gb) + " GB";
        } else if(size_mb > 0) {
            return String.format("%.3f", size_gb) + " MB";
        } else {
            return String.format("%.3f", size_gb) + " KB";
        }
    }

    public boolean isDownloaded(Object t) {
        String animeFolderName, animeName;
        if (t instanceof SplitNewRelease) {
            logger.debug("Object t is instance of SplitNewRelease");
            SplitNewRelease newRelease = (SplitNewRelease) t;
            animeFolderName = newRelease.getAnimeFolderName();
            animeName = newRelease.getAnimeName();
        }
        else if (t instanceof  SplitAlreadyReleased) {
            logger.debug("Object t is instance of SplitAlreadyReleased");
            SplitAlreadyReleased alreadyReleased = (SplitAlreadyReleased) t;
            animeFolderName = alreadyReleased.getAnimeFolderName();
            animeName = alreadyReleased.getAnimeName();
        }
        else {
            logger.error("Object t has wrong instance");
            return false;
        }
        File folder = new File(workPlace.getConf().getProperty("downloadFolder") + "/" + animeFolderName);
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;
        if (folder.exists()) {
            for (File file : listOfFiles) {
                if (animeName.contains(file.getName().toLowerCase())) {
                    return true;
                }
            }
        }
        else {
            logger.error("Folder didn't exist");
        }
        return false;
    }
}
