package sk.spedry.weebbotcollector.ircbot;

import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pircbotx.dcc.ReceiveFileTransfer;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.IncomingFileTransferEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;

import sk.spedry.weebbotcollector.ircbot.util.DownloadMessage;
import sk.spedry.weebbotcollector.ircbot.util.SplitAlreadyReleased;
import sk.spedry.weebbotcollector.ircbot.util.SplitNewRelease;
import sk.spedry.weebbotcollector.util.WCMAnime;
import sk.spedry.weebbotcollector.util.WCMAnimeName;
import sk.spedry.weebbotcollector.util.WCMProgress;
import sk.spedry.weebbotcollector.work.WBCWorkPlace;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Objects;

public class IRCBotListener extends ListenerAdapter {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Setter
    private String downloadFolder;
    private final WBCWorkPlace workPlace;
    private final IRCBotWorkPlace botWorkPlace;
    private final IRCBotCommands botCommands;
    private DownloadMessage currentlyDownloading;
    private ArrayList<DownloadMessage> downloadQueue = new ArrayList<DownloadMessage>();
    private ReceiveFileTransfer fileTransfer;


    // BOT SETTINGS
    @Setter
    // TODO ADD OPTIONS TO SET THIS VALUE FROM APP/INIT THIS VALUE WHEN BOT IS CREATED
    // one GB = 1 000 000 000 bytes for info DECIMAL = GB/1000
    // one GB = 1 073 741 824 bytes for info BINARY = GB/1024
    private long maxFileSize = 3000000000L; //2gb default
    private boolean disIpv6 = true;

    public IRCBotListener(String downloadFolder, WBCWorkPlace workPlace, IRCBotCommands botCommands) {
        this.downloadFolder = downloadFolder;
        this.workPlace = workPlace;
        this.botCommands = botCommands;
        this.botWorkPlace = new IRCBotWorkPlace();
    }

    /*@Override
    public void onGenericMessage(GenericMessageEvent event) throws Exception {
        logger.debug("Generic message: {}", event.getMessage());
    }*/

    @Override
    public void onMessage(MessageEvent event) {
        if (disIpv6 && Objects.requireNonNull(event.getUser()).getNick().toLowerCase().contains("ipv6")) {
            logger.debug("IPV6 is disabled");
            return;
        }

        final String receivedMessage = event.getMessage().toLowerCase();
        final String userName = event.getUser().getNick();
        String message = null;

        logger.debug("Received message: " + receivedMessage);

        // MATCH SEARCH BOT
        if (userName.equals(workPlace.getConf().getProperty("searchBot"))) {
            // download already released anime
            downloadAlreadyReleased(receivedMessage);
            return;
        }
        // else if () {}

        // MATCH RELEASE BOTS
        for (String releaseBotName : workPlace.getConf().getReleaseBotsList()) {
            if (userName.equals(releaseBotName)) {
                // Download released anime
                downloadNewRelease(receivedMessage);
                return;
            }
        }

        // MATCH RELEASE IPv6 BOTS
        if (!disIpv6 && !Objects.requireNonNull(event.getUser()).getNick().toLowerCase().contains("ipv6")) {
            for (String releaseBotNameIPv6 : workPlace.getConf().getReleaseBotsList()) {
                if (userName.equals(releaseBotNameIPv6)) {
                    // Download released anime
                    downloadNewRelease(receivedMessage);
                    return;
                }
            }
        }
        else {
            logger.debug("IPV6 is disabled");
        }

        logger.warn("Unknown user: {}, or couldn't parse received message: {}", userName, receivedMessage);
    }

    private void downloadNewRelease(String receivedMessage) {
        // Split message
        SplitNewRelease newRelease;
        try {
            newRelease = new SplitNewRelease(receivedMessage);
        } catch (Exception e) {
            logger.error("Couldn't parse message");
            return;
        }

        // get download message from
        logger.trace("Going through all anime entries in jsonListFile: animeList.json");
        try {
            boolean match = false;
            for (WCMAnime anime : workPlace.getAnimeList().getAnimeList()) {
                if (newRelease.getAnimeName().contains(anime.getTypeOfQuality().getName().toLowerCase())) {
                    if (newRelease.getAnimeName().contains(anime.getAnimeName().toLowerCase())) {
                        // TODO MATCH WITH BOT IF CHOSEN
                        // check if user specified bot from who our bot should be downloading
                        match = true;
                    }
                }
            }
            if (!match)
                throw new Exception("Anime didn't match");
        } catch (Exception e) {
            logger.error(e);
        }

        // Tests
        // 1. TEST IF ANIME ISN'T CURRENTLY BEING DOWNLOADED
        if (currentlyDownloading != null && newRelease.getAnimeName().contains(currentlyDownloading.getAnimeName())) {
            //TODO IF EXISTING FILE < ACTUAL FILE SIZE
            logger.debug("This anime {}, is currently being downloaded", newRelease.getAnimeName());
            return;
        }

        // 2. TEST IF ANIME ISN'T ALREADY IN QUEUE
        for (DownloadMessage downloadMsg : downloadQueue) {
            if (newRelease.getAnimeName().contains(downloadMsg.getAnimeName())) {
                logger.debug("This anime {}, is already queue", newRelease.getAnimeName());
                return;
            }
        }

        // 3.1 SET ANIME FOLDER NAME
        newRelease.setAnimeFolderName(workPlace.getAnimeList());

        // 3.2 TEST ANIME NAME
        if (newRelease.getAnimeFolderName() == null) {
            logger.error("Anime folder name was empty!");
            return;
        }

        // 3.3 TEST IF ANIME ISN'T ALREADY DOWNLOADED
        File folder = new File(downloadFolder + "/" + newRelease.getAnimeFolderName());
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;
        if (folder.exists()) {
            for (File file : listOfFiles) {
                if (receivedMessage.contains(file.getName().toLowerCase())) {
                    logger.debug("This anime {}, is already downloaded", newRelease.getAnimeName());
                    return;
                }
            }
        }

        // 4. TEST IF IS SOMETHING IS BEING DOWNLOADED
        if (fileTransfer != null && fileTransfer.getFileTransferStatus().isAlive()) {
            // 4.1 Add anime into download queue
            downloadQueue.add(new DownloadMessage(
                    newRelease.getDownloadMessage().getBotName(),
                    newRelease.getDownloadMessage().getMessage(),
                    newRelease.getAnimeName()));
        }
        else {
            // 4.2 Send download message
            botCommands.sendMessage(
                    newRelease.getDownloadMessage().getBotName(),
                    newRelease.getDownloadMessage().getMessage());
            // 4.3 set currentlyDownloading anime
            currentlyDownloading = new DownloadMessage(
                    newRelease.getDownloadMessage().getBotName(),
                    newRelease.getDownloadMessage().getMessage(),
                    newRelease.getAnimeName());
        }
    }

    private void downloadAlreadyReleased(String receivedMessage) {
        // Split message
        SplitAlreadyReleased alreadyReleased;
        try {
            alreadyReleased = new SplitAlreadyReleased(receivedMessage);
        }
        catch (Exception e) {
            logger.error("Couldn't parse message: {}", receivedMessage);
            return;
        }
        // 1. TEST IF IS SOMETHING IS BEING DOWNLOADED
        if (fileTransfer != null && fileTransfer.getFileTransferStatus().isAlive()) {
            // 1.1 TEST IF THIS ALREADY RELEASED ANIME ISN'T BEING DOWNLOADED
            if (currentlyDownloading.getAnimeName().equals(alreadyReleased.getDownloadMessage().getAnimeName()))
                return;
            // 1.2 Add anime into download queue
            downloadQueue.add(new DownloadMessage(
                    alreadyReleased.getDownloadMessage().getBotName(),
                    alreadyReleased.getDownloadMessage().getMessage(),
                    alreadyReleased.getAnimeName()));
        }
        else {
            // 1.3 Send download message
            botCommands.sendMessage(
                    alreadyReleased.getDownloadMessage().getBotName(),
                    alreadyReleased.getDownloadMessage().getMessage()
            );
            // 1.4 set currentlyDownloading anime
            currentlyDownloading = new DownloadMessage(
                    alreadyReleased.getDownloadMessage().getBotName(),
                    alreadyReleased.getDownloadMessage().getMessage(),
                    alreadyReleased.getAnimeName());
        }

        // To make sure stop receiving form search bot
        botCommands.sendMessage(
                workPlace.getConf().getProperty("searchBot"),
                "STOP"
        );
    }

    //public void onPrivateMessage(PrivateMessageEvent event) {
        // TODO OPTION TO DOWNLOAD ANIME BY SENDING PRIVATE MESSAGE TO BOT
    //}
    @Override
    public void onPrivateMessage(PrivateMessageEvent event) {
        // DONT ADD
        if (!Objects.requireNonNull(event.getUser()).toString().toLowerCase().contains("spedry"))
            return;
        // THIS

        if (disIpv6 && Objects.requireNonNull(event.getUser()).getNick().toLowerCase().contains("ipv6")) {
            logger.debug("IPV6 is disabled");
            return;
        }

        final String receivedMessage = event.getMessage().toLowerCase();
        final String userName = event.getUser().getNick();
        String message = null;

        logger.debug("Received message: " + receivedMessage);

        // MATCH SEARCH BOT
        if (userName.equals(workPlace.getConf().getProperty("searchBot"))) {
            // download already released anime
            downloadAlreadyReleased(receivedMessage);
            return;
        }
        // else if () {}

        // MATCH RELEASE BOTS
        for (String releaseBotName : workPlace.getConf().getReleaseBotsList()) {
            if (userName.equals(releaseBotName)) {
                // Download released anime
                downloadNewRelease(receivedMessage);
                return;
            }
        }

        // MATCH RELEASE IPv6 BOTS
        if (!disIpv6 && !Objects.requireNonNull(event.getUser()).getNick().toLowerCase().contains("ipv6")) {
            for (String releaseBotNameIPv6 : workPlace.getConf().getReleaseBotsList()) {
                if (userName.equals(releaseBotNameIPv6)) {
                    // Download released anime
                    downloadNewRelease(receivedMessage);
                    return;
                }
            }
        }
        else {
            logger.debug("IPV6 is disabled");
        }

        logger.warn("Unknown user: {}, or couldn't parse received message: {}", userName, receivedMessage);
        /*
        // TODO SPLICE INTO SMALLER PARTS
        if (receivedMessage.contains("/msg")) {
            // TODO CHANGE IT
            SplitNewRelease splitNewRelease = null;
            try {
                splitNewRelease = new SplitNewRelease(receivedMessage);
            } catch (Exception e) {
                logger.error("Couldn't parse message");
            }
            if (splitNewRelease == null)
                return;
            logger.trace("Going through all anime entries in jsonListFile: animeList.json");
            for (WCMAnime anime : workPlace.getAnimeList().getAnimeList()) {
                if (splitNewRelease.getAnimeName().contains(anime.getTypeOfQuality().getName().toLowerCase())) {
                    if (splitNewRelease.getAnimeName().contains(anime.getAnimeName().toLowerCase())) {
                        // TODO MATCH WITH BOT IF CHOSEN
                        // check if user specified bot from who our bot should be downloading
                        message = receivedMessage.substring(receivedMessage.lastIndexOf("/msg"));
                        break;
                    }
                }
            }

            if (message != null) {
                // TEST IF ANIME ISN'T CURRENTLY BEING DOWNLOADED
                if (currentlyDownloading != null && splitNewRelease.getAnimeName().contains(currentlyDownloading.getAnimeName())) {
                    //TODO IF EXISTING FILE < ACTUAL FILE SIZE
                    logger.debug("This anime {}, is currently being downloaded", splitNewRelease.getAnimeName());
                    return;
                }

                // TEST IF ANIME ISN'T ALREADY IN QUEUE
                for (DownloadMessage downloadMessage : downloadQueue) {
                    if (splitNewRelease.getAnimeName().contains(downloadMessage.getAnimeName())) {
                        logger.debug("This anime {}, is already queue", splitNewRelease.getAnimeName());
                        return;
                    }
                }

                // GET ANIME NAME
                String animeName = null;
                for (WCMAnime anime : workPlace.getAnimeList().getAnimeList()) {
                    if (receivedMessage.contains(anime.getAnimeName().toLowerCase())) {
                        animeName = anime.getAnimeName();
                        break;
                    }
                }
                if (animeName == null)
                    return;

                // TEST IF ANIME ISN'T ALREADY DOWNLOADED
                File folder = new File(downloadFolder + "/" + animeName);
                File[] listOfFiles = folder.listFiles();
                assert listOfFiles != null;
                if (folder.exists()) {
                    for (File file : listOfFiles) {
                        if (receivedMessage.contains(file.getName().toLowerCase())) {
                            logger.debug("This anime {}, is already downloaded", splitNewRelease.getAnimeName());
                            return;
                        }
                    }
                }

                // TEST IF IS SOMETHING IS BEING DOWNLOADED
                if (fileTransfer != null && fileTransfer.getFileTransferStatus().isAlive()) {
                    // ADD ANIME INTO DOWNLOAD QUEUE
                    downloadQueue.add(new DownloadMessage(
                            splitNewRelease.getDownloadMessage().getBotName(),
                            splitNewRelease.getDownloadMessage().getMessage(),
                            splitNewRelease.getAnimeName()));
                }
                else {
                    // SEND DOWNLOAD MESSAGE
                    botCommands.sendMessage(
                            splitNewRelease.getDownloadMessage().getBotName(),
                            splitNewRelease.getDownloadMessage().getMessage());
                    currentlyDownloading = new DownloadMessage(
                            splitNewRelease.getDownloadMessage().getBotName(),
                            splitNewRelease.getDownloadMessage().getMessage(),
                            splitNewRelease.getAnimeName());
                }
                // TODO do some more tests
                for (DownloadMessage downloadMessage : downloadQueue)
                    logger.debug("Anime in download queue: {}", downloadMessage.getAnimeName());
            }
        }*/
    }

    @Override
    public void onIncomingFileTransfer(IncomingFileTransferEvent event) throws Exception {
        logger.debug("On incoming file transfer method started");
        super.onIncomingFileTransfer(event);
        // RECEIVED FILE NAME
        String receivedFileName = event.getSafeFilename();
        // ANIME NAME FROM ANIME LIST
        String animeName;
        // FILE SIZE OF RECEIVED (Anime) FILE
        long fileSize = event.getFilesize();

        // SOME TESTS BEFORE DOWNLOAD
        logger.debug("Testing if maxFileSize isn't smaller zero");
        if (maxFileSize < 0) {
            logger.error("Variable maxFileSize is smaller zero");
            return;
        }
        logger.debug("Testing if file isn't over file size limit");
        if (event.getFilesize() > maxFileSize) {
            logger.warn("Received file: {} went over max file size limit: {}!", receivedFileName, event.getFilesize());
            return;
        }
        logger.debug("Testing if received file contains name of anime from download list");
        testReceivedFile: try {
            for (WCMAnime anime : workPlace.getAnimeList().getAnimeList()) {
                if (receivedFileName.contains(anime.getAnimeName())) {
                    animeName = anime.getAnimeName();
                    break testReceivedFile;
                }
            }
            throw new Exception();
        } catch (Exception e) {
            logger.error("This file: {} didn't match any anime from download anime list", receivedFileName);
            return;
        }

        // CREATE ANIME FOLDER WHERE ALL ANIME EP WILL BE PLACED
        // IF ANIME FOLDER EXISTS DO NOTHING
        workPlace.createFolder(this.downloadFolder + "/" + animeName);
        Path path = Paths.get(downloadFolder + "/" + receivedFileName);

        // TODO IF ANIME EXISTS CHECK IF IS FULLY DOWNLOADED OR DOWNLOAD REST
        if (path.toFile().exists()) {
            // TODO IF TO TEST IS EXISTING FILE == event.fileSize();
            logger.debug("File already exists, resuming where ended");
            fileTransfer = event.acceptResume(path.toFile(), Files.readAttributes(path, BasicFileAttributes.class).size());
        }
        // ELSE ACCEPT FILE TRANSFER
        else {
            logger.debug("Accepting file transfer");
            fileTransfer = event.accept(path.toFile());
        }
        
        // THREAD THAT WILL KEEP AN EYE ON DOWNLOAD
        Thread progressThread = new Thread(() -> {
            // TELL CLIENT THAT THIS ANIME IS BEING DOWNLOADED
            workPlace.send("setDownloadingAnimeName", new WCMAnimeName(animeName));
            // CYCLE TO INFORM CLIENT ABOUT DOWNLOAD PROGRESS
            while (!fileTransfer.getFileTransferStatus().isFinished()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //logger.debug("Downloaded: {}", fileTransfer.getFileTransferStatus().getBytesTransfered());
                double progress = (double) fileTransfer.getFileTransferStatus().getBytesTransfered() / fileSize;
                // SEND PROGRESS
                workPlace.send("setProgress", new WCMProgress(progress));
            }
            // IF FILE TRANSFER WAS SUCCESSFUL
            if (fileTransfer.getFileTransferStatus().isSuccessful()) {
                logger.debug("Clearing currently downloading variable");
                currentlyDownloading = null;
                workPlace.increaseAnimeDownload(animeName);
                // START ANOTHER DOWNLOAD IF THERE IS ANY IN QUEUE
                if (!downloadQueue.isEmpty()) {
                    logger.debug("Starting another download");
                    botCommands.sendMessage(downloadQueue.remove(0));
                    workPlace.send("setDownloadingAnimeName", new WCMAnimeName(""));
                }
            }
            // TODO SOMETHING WENT WRONG
            else {
                logger.error("FILE TRANSFER WASN'T SUCCESSFUL");
            }
        });
        progressThread.setName("FileTransferStatus");

        progressThread.start();
        logger.debug("Transfer of {} started, with {} size", animeName, botWorkPlace.bytesToReadable(fileSize));
        //TODO AFTER SOME TIME TEST IF DOWNLOAD STARTED OR NOT
        // IF NOT CLEAR CURRENT DOWNLOAD VARIABLE
        fileTransfer.transfer();
    }
}
