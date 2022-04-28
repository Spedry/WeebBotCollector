package sk.spedry.weebbotcollector.ircbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.spedry.weebbotcollector.ircbot.util.DownloadMessage;
import sk.spedry.weebbotcollector.ircbot.util.SplitAlreadyReleased;
import sk.spedry.weebbotcollector.ircbot.util.SplitNewRelease;
import sk.spedry.weebbotcollector.work.WBCWorkPlace;

import java.io.File;
import java.util.ArrayList;

public class testFunctions extends IRCBotListener {

    private final Logger logger = LogManager.getLogger(this.getClass());

    public testFunctions(WBCWorkPlace workPlace, IRCBotCommands botCommands) {
        super(workPlace, botCommands);
    }

    public boolean testIfBotIsAllowed(SplitAlreadyReleased alreadyReleased) {
        logger.traceEntry();
        for (String alreadyReleasedBotName : getWorkPlace().getConf().getAlreadyReleasedBotsList()) {
            logger.debug("AlreadyReleasedBotName: {} vs. downloadMsg: {}", alreadyReleasedBotName, alreadyReleased.getDownloadMessage().getBotName());
            if (alreadyReleased.getDownloadMessage().getBotName().equalsIgnoreCase(alreadyReleasedBotName)) {
                return logger.traceExit(true);
            }
        }
        return logger.traceExit(false);

    }

    public boolean testIfAnimeIsAlreadyDownloaded(Object t) {
        logger.traceEntry();
        String animeFolderName, animeName, animeQuality;
        if (t instanceof SplitNewRelease) {
            logger.debug("Object t is instance of SplitNewRelease");
            SplitNewRelease newRelease = (SplitNewRelease) t;
            animeFolderName = newRelease.getAnimeFolderName();
            animeName = newRelease.getDownloadMessage().getAnimeName();
            animeQuality = newRelease.getDownloadMessage().getAnimeQuality();
        }
        else if (t instanceof  SplitAlreadyReleased) {
            logger.debug("Object t is instance of SplitAlreadyReleased");
            SplitAlreadyReleased alreadyReleased = (SplitAlreadyReleased) t;
            animeFolderName = alreadyReleased.getAnimeFolderName();
            animeName = alreadyReleased.getDownloadMessage().getAnimeName();
            animeQuality = alreadyReleased.getDownloadMessage().getAnimeQuality();
        }
        else {
            logger.error("Object t has wrong instance");
            return logger.traceExit( false);
        }

        logger.debug(animeFolderName);
        logger.debug(animeName);
        logger.debug(animeQuality);

        File folder = new File(getWorkPlace().getConf().getProperty("downloadFolder") + "/" + animeFolderName);
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;
        logger.debug("Testing if anime is inside folder was downloaded");
        if (folder.exists()) {
            logger.debug("Folder {} existed", folder.getPath());
            for (File file : listOfFiles) {
                //logger.debug("Testing file {}, with {}", file.getName(), animeName);
                if (file.getName().toLowerCase().contains(animeName)) {
                    logger.debug("{} was in folder", animeName);
                    if (file.getName().toLowerCase().contains(animeQuality)) {
                        logger.debug("{} quality of anime matched", animeQuality);
                        return logger.traceExit( true);
                    }
                }
            }
        }
        else {
            logger.error("Folder didn't exist");
        }
        return logger.traceExit( false);
    }

    public boolean testIfAnimeIsInReleaseQueue(SplitAlreadyReleased alreadyReleased) {
        logger.traceEntry();
        for (DownloadMessage downloadMessage : getAlreadyReleasedQueue()) {
            if (downloadMessage.getAnimeFileName().equals(alreadyReleased.getDownloadMessage().getAnimeFileName())) {
                logger.warn("This anime {}, is already in release queue", alreadyReleased.getAnimeName());
                return logger.traceExit( true);
            }
        }
        return logger.traceExit( false);
    }

    public boolean testIfAnimeIsInDownloadQueue(SplitNewRelease newRelease) {
        logger.traceEntry();
        for (DownloadMessage downloadMessage : getDownloadQueue()) {
            if (downloadMessage.getAnimeFileName().equals(newRelease.getDownloadMessage().getAnimeFileName())) {
                logger.info("This anime {}, is already in download queue", newRelease.getAnimeName());
                return logger.traceExit( true);
            }
        }
        return logger.traceExit( false);
    }

    public boolean testIfAnimeIsNotCurrentlyBeingDownloaded(SplitNewRelease newRelease) {
        logger.traceEntry();
        if (getCurrentlyDownloading() != null && newRelease.getAnimeName().contains(getCurrentlyDownloading().getAnimeFileName())) {
            //TODO IF EXISTING FILE < ACTUAL FILE SIZE
            logger.debug("This anime {}, is currently being downloaded", newRelease.getAnimeName());
            return logger.traceExit( true);
        }
        return logger.traceExit( false);
    }



}
