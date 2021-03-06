package sk.spedry.weebbotcollector.ircbot;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pircbotx.User;
import org.pircbotx.dcc.ReceiveFileTransfer;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.FileTransferCompleteEvent;
import org.pircbotx.hooks.events.IncomingFileTransferEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;

import sk.spedry.weebbotcollector.ircbot.util.DownloadMessage;
import sk.spedry.weebbotcollector.ircbot.util.SplitAlreadyReleased;
import sk.spedry.weebbotcollector.ircbot.util.SplitNewRelease;
import sk.spedry.weebbotcollector.util.WCMAnime;
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
    @Getter
    private final WBCWorkPlace workPlace;
    private final IRCBotWorkPlace botWorkPlace;
    private final IRCBotCommands botCommands;
    @Getter
    private DownloadMessage currentlyDownloading;
    @Getter
    private final ArrayList<DownloadMessage> downloadQueue = new ArrayList<>();
    @Getter
    private final ArrayList<DownloadMessage> alreadyReleasedQueue = new ArrayList<>();
    public static Thread downloadAllInAlreadyReleasedQueue;
    private ReceiveFileTransfer fileTransfer;
    private final testFunctions testFunctions;

    // BOT SETTINGS
    @Setter
    // TODO ADD OPTIONS TO SET THIS VALUE FROM APP/INIT THIS VALUE WHEN BOT IS CREATED
    // one GB = 1 000 000 000 bytes for info DECIMAL = GB/1000
    // one GB = 1 073 741 824 bytes for info BINARY = GB/1024
    private long maxFileSize = 3000000000L; //2gb default
    private boolean disIpv6 = true;

    public IRCBotListener(WBCWorkPlace workPlace, IRCBotCommands botCommands) {
        logger.traceEntry();
        this.workPlace = workPlace;
        this.botCommands = botCommands;
        botCommands.setBotListener(this);
        this.botWorkPlace = new IRCBotWorkPlace(workPlace);
        this.testFunctions = new testFunctions(this);
        downloadAllInAlreadyReleasedQueue = new Thread(() -> {
            try {
                while (true) {
                    logger.debug("Waiting");
                    synchronized (IRCBotListener.class) {
                        IRCBotListener.class.wait();
                    }
                    logger.debug("Waiting ended");
                    if (!alreadyReleasedQueue.isEmpty()) {
                        logger.debug("Removing older versions");
                        ArrayList<DownloadMessage> tempAlreadyReleasedQueue = new ArrayList<>(alreadyReleasedQueue);
                            for (DownloadMessage downloadMessage1 : tempAlreadyReleasedQueue) {
                                for (DownloadMessage downloadMessage2 : tempAlreadyReleasedQueue) {
                                    if (downloadMessage1.getEpisodeNumber().getEpisodeNumber() == downloadMessage2.getEpisodeNumber().getEpisodeNumber()) {
                                        if (downloadMessage1.getEpisodeNumber().getEpisodeCode() < downloadMessage2.getEpisodeNumber().getEpisodeCode()) {
                                            alreadyReleasedQueue.remove(downloadMessage1);
                                        }
                                    }
                                }
                            }

                        logger.info("These anime will be downloaded");
                        for (DownloadMessage downloadMessage : alreadyReleasedQueue) {
                            logger.info(downloadMessage.getAnimeFileName());
                        }
                        if (fileTransfer == null || !fileTransfer.getFileTransferStatus().isAlive()) {
                            DownloadMessage downloadMessage = alreadyReleasedQueue.remove(0);
                            logger.debug("Nothing is being downloaded, begin to download {}", downloadMessage.getAnimeFileName());
                            sendMessage(downloadMessage);
                        }
                        if (!alreadyReleasedQueue.isEmpty()) {
                            logger.debug("Putting rest into download queue");
                            downloadQueue.addAll(alreadyReleasedQueue);
                            alreadyReleasedQueue.clear();
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        downloadAllInAlreadyReleasedQueue.setDaemon(true);
        downloadAllInAlreadyReleasedQueue.start();
        logger.traceExit();
    }

    /**************************BOT EVENTS**************************/
    // ONLY SEARCHBOT
    @Override
    public void onGenericMessage(GenericMessageEvent event) {
        if (workPlace.getConf().getBoolProperty("traceReceivedMessage"))
            logger.traceEntry(event.getMessage());
        else
            logger.traceEntry();
        if (event.getUser().getNick().equals(workPlace.getConf().getProperty("searchBot")))
            processMessage(event.getUser(), event.getMessage());
        logger.traceExit();
    }
    // ALL OTHERS
    @Override
    public void onMessage(MessageEvent event) {
        if (workPlace.getConf().getBoolProperty("traceReceivedMessage"))
            logger.traceEntry(event.getMessage());
        else
            logger.traceEntry();
        processMessage(Objects.requireNonNull(event.getUser()), event.getMessage());
        logger.traceExit();
    }
    // PRIVATE MSG LIKE FROM MY SELF
    @Override
    public void onPrivateMessage(PrivateMessageEvent event) {
        logger.traceEntry("From: {} : {}", Objects.requireNonNull(event.getUser()).toString(), event.getMessage());
        // DONT ADD
        if (!Objects.requireNonNull(event.getUser()).toString().toLowerCase().contains("spedry"))
            return;
        // THIS

        processMessage(event.getUser(), event.getMessage());
        logger.traceExit();
    }

    @Override
    public void onIncomingFileTransfer(IncomingFileTransferEvent event) throws Exception {
        logger.traceEntry(event.getSafeFilename());
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
        workPlace.createFolder(workPlace.getConf().getProperty("downloadFolder") + "/" + animeName);
        Path path = Paths.get(workPlace.getConf().getProperty("downloadFolder") + "/" + animeName + "/" + receivedFileName);
        logger.info("Path is: {}", path);
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
            workPlace.send("setCurrentlyDownloadingAnime", currentlyDownloading.getAnimeName());
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
                workPlace.send("setProgress", progress);
                //logger.info("Downloaded: {}", fileTransfer.getFileTransferStatus().getPercentageComplete());
            }
            // IF FILE TRANSFER WAS SUCCESSFUL
            if (fileTransfer.getFileTransferStatus().isSuccessful()) {
                logger.debug("Setting number of downloaded episodes for {}", animeName);
                workPlace.updateNumberOfDownloadedEpisodes(animeName);

                logger.debug("Setting wasDownloaded to true for {}", animeName);
                workPlace.setWasDownloaded(animeName, true);

                if (currentlyDownloading.isWillSetReleaseDate()) {
                    logger.debug("Setting releaseDate to today's date for {}", animeName);
                    workPlace.setReleaseDate(animeName);
                    logger.debug("Setting setReleaseDate boolean to: false");
                }
                else {
                    logger.debug("This download didn't start onMessage, not actual release date");
                }

                logger.debug("Clearing currently downloading variable");
                clearCurrentlyDownloading();

                // START ANOTHER DOWNLOAD IF THERE IS ANY IN QUEUE
                if (!downloadQueue.isEmpty()) {
                    logger.debug("Starting another download");
                    sendMessage(downloadQueue.remove(0));
                }
            }
            // TODO SOMETHING WENT WRONG
            else {
                logger.error("FILE TRANSFER WASN'T SUCCESSFUL");
                logger.error("Anime that failed is {}", currentlyDownloading.getAnimeFileName());
                try {
                    Thread.sleep(10000);
                    logger.debug("Repeating downloading of {}", currentlyDownloading.getAnimeFileName());
                    sendMessage(currentlyDownloading);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            workPlace.send("setCurrentlyDownloadingAnime", "");
        });
        progressThread.setName("FileTransferStatus");

        progressThread.start();
        logger.debug("Transfer of {} started, with {} size", animeName, botWorkPlace.bytesToReadable(fileSize));
        // TODO IF THREAD FROM sendMessage(downloadMessage) IS STILL RUNNING DONT CREATE NEW ONE. BUT EXTEND TIME ????
        Thread testIfAnimeDownloadStarted = createTestThread();
        logger.debug("Starting thread to test if anime download started");
        testIfAnimeDownloadStarted.start();

        fileTransfer.transfer();
        logger.traceExit(fileTransfer.getFileTransferStatus().isSuccessful());
    }

    @Override
    public void onFileTransferComplete(FileTransferCompleteEvent event) {
        logger.info("File transfer of {} ended, from user: {}", event.getFileName(), event.getUser());
    }

    /**************************FUNCTIONS**************************/

    private void processMessage(User user, String message) {
        logger.traceEntry("From: {} : {}", user.toString(), message);
        if (disIpv6 && Objects.requireNonNull(user).getNick().toLowerCase().contains("ipv6")) {
            logger.debug("IPV6 is disabled");
            return;
        }

        final String userName = user.getNick();

        //logger.debug("Received message: " + receivedMessage);

        // MATCH SEARCH BOT
        if (userName.equals(workPlace.getConf().getProperty("searchBot"))) {
            // download already released anime
            downloadAlreadyReleased(message);
            return;
        }
        // else if () {}

        // MATCH RELEASE BOTS
        for (String releaseBotName : workPlace.getConf().getReleaseBotsList()) {
            if (userName.equals(releaseBotName)) {
                // Download released anime
                downloadNewRelease(message);
                return;
            }
        }

        // MATCH RELEASE IPv6 BOTS
        if (!disIpv6 && !Objects.requireNonNull(user).getNick().toLowerCase().contains("ipv6")) {
            for (String releaseBotNameIPv6 : workPlace.getConf().getReleaseBotsList()) {
                if (userName.equals(releaseBotNameIPv6)) {
                    // Download released anime
                    downloadNewRelease(message);
                    return;
                }
            }
        }
        else {
            logger.debug("IPV6 is disabled");
        }
        if (workPlace.getConf().getBoolProperty("logUnknownUsers"))
            logger.debug("WARN Unknown user: {}, or couldn't parse received message: {} WARN", userName, message);
        logger.traceExit();
    }

    private void downloadNewRelease(String receivedMessage) {
        logger.traceEntry();
        // Split message
        SplitNewRelease newRelease;
        try {
            newRelease = new SplitNewRelease(receivedMessage);
        } catch (Exception e) {
            logger.error("Couldn't parse message");
            return;
        }

        // get download message from
        logger.trace("Going through all anime entries in jsonListFile: {}", workPlace.getAnimeListFile());
        try {
            boolean match = false;
            for (WCMAnime anime : workPlace.getAnimeList().getAnimeList()) {
                if (newRelease.getDownloadMessage().getAnimeFileName().contains(anime.getTypeOfQuality().getName().toLowerCase())) {
                    if (newRelease.getDownloadMessage().getAnimeFileName().contains(anime.getAnimeName().toLowerCase())) {
                        // TODO MATCH WITH BOT IF CHOSEN
                        // check if user specified bot from who our bot should be downloading
                        match = true;
                    }
                }
            }
            if (!match)
                throw new Exception();
        } catch (Exception e) {
            logger.error("Anime didn't match with any anime in jsonListFile: {}", workPlace.getAnimeListFile());
            return;
        }

        // Tests
        // 1. TEST IF ANIME ISN'T CURRENTLY BEING DOWNLOADED
        if (testFunctions.testIfAnimeIsNotCurrentlyBeingDownloaded(newRelease))
            return;

        // 2. TEST IF ANIME ISN'T ALREADY IN QUEUE
        if (testFunctions.testIfAnimeIsInDownloadQueue(newRelease))
            return;

        //Set anime folder name
        newRelease.setAnimeFolderName(workPlace.getAnimeList(), newRelease.getDownloadMessage().getAnimeFileName());

        // 3.1 TEST ANIME NAME
        if (newRelease.getAnimeFolderName() == null) {
            logger.error("Anime folder name was empty!");
            return;
        }

        // 3.2 TEST IF ANIME ISN'T ALREADY DOWNLOADED
        if (testFunctions.testIfAnimeIsAlreadyDownloaded(newRelease)) {
            logger.debug("This anime {}, is already downloaded", newRelease.getDownloadMessage().getAnimeFileName());
            return;
        }

        //Setting setReleaseDate to true
        logger.debug("Setting setReleaseDate boolean to: true (that means all test went smoothly)");
        newRelease.getDownloadMessage().setWillSetReleaseDate(true);

        // 4. TEST IF IS SOMETHING IS BEING DOWNLOADED
        if (fileTransfer != null && fileTransfer.getFileTransferStatus().isAlive()) {
            logger.debug("Adding {} into download queue", newRelease.getDownloadMessage().getAnimeFileName());
            // 5.1 Add anime into download queue
            downloadQueue.add(newRelease.getDownloadMessage());
        }
        else {
            logger.debug("Sending message to download {}", newRelease.getDownloadMessage().getAnimeFileName());
            // 5.2 Send download message
            sendMessage(newRelease.getDownloadMessage());
        }
        logger.traceExit();
    }

    private void downloadAlreadyReleased(String receivedMessage) {
        logger.traceEntry();
        // Split message
        SplitAlreadyReleased alreadyReleased;
        try {
            alreadyReleased = new SplitAlreadyReleased(receivedMessage);
        }
        catch (Exception e) {
            if (receivedMessage.contains("sending results..."))
                logger.info("Starting receiving already released episodes");
            else
                logger.error("Couldn't parse message: {}", receivedMessage);
            return;
        }

        if (disIpv6 && Objects.requireNonNull(alreadyReleased.getDownloadMessage().getBotName()).toLowerCase().contains("ipv6")) {
            logger.debug("IPV6 is disabled");
            return;
        }

        //Test
        // 1. TEST IF IT'S ALLOWED BOT
        logger.debug("Testing if bot bot is allowed");
        if (!testFunctions.testIfBotIsAllowed(alreadyReleased))
            return;

        // 2.1 SET ANIME FOLDER NAME
        logger.debug("Setting anime folder name");
        alreadyReleased.setAnimeFolderName(workPlace.getAnimeList(), alreadyReleased.getDownloadMessage().getAnimeFileName());
        // 2.2 TEST ANIME NAME
        logger.debug("Test if anime folder name is empty");
        if (alreadyReleased.getAnimeFolderName() == null) {
            logger.warn("Anime folder name was empty!");
            return;
        }
        // 2.3 TEST IF ANIME ISN'T ALREADY DOWNLOADED
        logger.debug("Test if anime isn't already downloaded");
        //TODO DOESNT WORK
        if (testFunctions.testIfAnimeIsAlreadyDownloaded(alreadyReleased)) {
            logger.info("This anime {}, is already downloaded", alreadyReleased.getDownloadMessage().getAnimeFileName());
            File file = new File(workPlace.getConf().getProperty("downloadFolder") + "/" + alreadyReleased.getAnimeFolderName());

            if (Objects.requireNonNull(file.list()).length > workPlace.getAnime(alreadyReleased.getAnimeFolderName()).getNumberOfDownloadedEpisodes()) {
                workPlace.updateNumberOfDownloadedEpisodes(alreadyReleased.getAnimeFolderName());
            }
            else {
                logger.debug("Anime downloaded episodes ({}) => ({}) anime in folder",
                        workPlace.getAnime(alreadyReleased.getAnimeFolderName()).getNumberOfDownloadedEpisodes(),
                        Objects.requireNonNull(file.list()).length);
            }
            return;
        }
        // 3. TEST IF ANIME ISN'T ALREADY IN RELEASED QUEUE
        logger.debug("Test if anime isn't already in release queue");
        if (testFunctions.testIfAnimeIsInReleaseQueue(alreadyReleased))
            return;

        logger.debug("Adding {} anime into release list", alreadyReleased.getDownloadMessage().getAnimeFileName());
        alreadyReleasedQueue.add(alreadyReleased.getDownloadMessage());
        logger.traceExit();
    }

    /**************************METHODS**************************/

    private void sendMessage(DownloadMessage downloadMessage) {
        logger.traceEntry();
        botCommands.sendMessage(downloadMessage);
        setCurrentlyDownloading(downloadMessage);
        // TODO AFTER SENDING THIS THREAD IS CREATED
        Thread testIfAnimeDownloadStarted = createTestThread();
        logger.debug("Starting thread to test if anime download started");
        testIfAnimeDownloadStarted.start();
        logger.traceExit();
    }

    // TODO ADD FUNCTION SO ONLY ONE THREAD IS CREATED
    private Thread createTestThread() {
        logger.traceEntry();
        return logger.traceExit(new Thread(() -> {
            try {
                logger.trace("Thread to test download started");
                Thread.sleep(Long.parseLong(workPlace.getConf().getProperty("waitBeforeRestartingDownload")));
                // TODO INFORM CLIENT SIDE
                if (fileTransfer != null && fileTransfer.getFileTransferStatus().isAlive()) {
                    logger.info("Download of anime {} started", currentlyDownloading.getAnimeFileName());
                } else {
                    //TODO after many failed attempts stop
                    // Add attempts counter
                    // if n attempts pause for seconds then send OR restart bot
                    logger.error("Download of anime {} didn't start", currentlyDownloading.getAnimeFileName());
                    // TODO MORE TESTING
                    try {
                        fileTransfer.shutdown();
                    }
                    catch (Exception e) {
                        logger.error("Couldn't shutdown fileTransfer: {}", e.getMessage());
                    }
                    sendMessage(currentlyDownloading);
                }
                logger.trace("Thread to test download ended");
            } catch (InterruptedException e) {
                logger.error(e);
            }
        }));
    }

    private void clearCurrentlyDownloading() {
        this.currentlyDownloading = null;
    }

    private void setCurrentlyDownloading(@NonNull DownloadMessage downloadMessage) {
        logger.traceEntry();
        if (this.currentlyDownloading == null) {
            this.currentlyDownloading = downloadMessage;
            logger.traceExit("Replaced:true Was equal:false");
        }
        else if (currentlyDownloading.getAnimeName().equals(downloadMessage.getAnimeName())) {
            logger.debug("Variable currentlyDownloading won't be replaced with the same download message");
            logger.traceExit("Replaced:false Was equal:true");
        }
        else {
            logger.error("Couldn't replace currentlyDownloading var, was not empty: tried to replace {} with {}", currentlyDownloading.getAnimeFileName(), downloadMessage.getAnimeFileName());
            logger.traceExit("Replaced:false Was equal:false");
        }
    }
}
