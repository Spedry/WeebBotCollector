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
import sk.spedry.weebbotcollector.ircbot.util.SplittedMessage;
import sk.spedry.weebbotcollector.util.WCMAnime;
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
    private long maxFileSize = 2000000000; //2gb default
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
        logger.info("Bots name: {}", Objects.requireNonNull(event.getUser()).getNick());
        if (disIpv6 && Objects.requireNonNull(event.getUser()).getNick().toLowerCase().contains("ipv6")) {
            logger.debug("IPV6 is disabled");
            return;
        }

        final String receivedMessage = event.getMessage().toLowerCase();
        String message = null;

        if (logger.isDebugEnabled())
            logger.debug("Received message: " + receivedMessage);

        if (receivedMessage.contains("/msg")) {
            SplittedMessage splittedMessage = new SplittedMessage(receivedMessage);

            logger.trace("Going through all anime entries in jsonListFile: animeList.json");
            for (WCMAnime anime : workPlace.getAnimeList().getAnimeList()) {
                if (splittedMessage.getAnimeName().contains(anime.getTypeOfQuality().getName().toLowerCase())) {
                    if (splittedMessage.getAnimeName().contains(anime.getAnimeName().toLowerCase())) {
                        // TODO MATCH WITH BOT IF CHOSEN
                        // check if user specified bot from who our bot should be downloading
                        message = receivedMessage.substring(receivedMessage.lastIndexOf("/msg"));
                        break;
                    }
                }
            }

            if (message != null) {
                // TEST IF ANIME ISN'T CURRENTLY BEING DOWNLOADED
                if (currentlyDownloading != null && splittedMessage.getAnimeName().contains(currentlyDownloading.getAnimeName())) {
                    logger.debug("This anime {}, is currently being downloaded", splittedMessage.getAnimeName());
                    return;
                }

                //TODO CHANGE TO TEST IF CURRENT ANIME ISN'T THE SAME
                /*for (AlreadyDownloadingAnime ADA : alreadyDownloadingAnime) {
                    if (ADA.getMessage().contains(message)) {
                        logger.debug("This anime {}, is already being downloaded", splittedMessage.getAnimeName());
                        return;
                    }
                }*/

                //TODO ADD TEST IF ANIME ISN'T IN QUEUE

                File folder = new File(downloadFolder);
                File[] listOfFiles = folder.listFiles();
                assert listOfFiles != null;
                for (File file : listOfFiles) {
                    if (file.getName().contains(receivedMessage)) {
                        logger.debug("This anime {}, is already downloaded", splittedMessage.getAnimeName());
                        return;
                    }
                }

                botCommands.sendMessage(
                        splittedMessage.getDownloadMessage().getBotName(),
                        splittedMessage.getDownloadMessage().getMessage());
            }
        }
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
        String message = null;

        logger.debug("Received message: " + receivedMessage);

        if (receivedMessage.contains("/msg")) {
            SplittedMessage splittedMessage = new SplittedMessage(receivedMessage);

            logger.trace("Going through all anime entries in jsonListFile: animeList.json");
            for (WCMAnime anime : workPlace.getAnimeList().getAnimeList()) {
                if (splittedMessage.getAnimeName().contains(anime.getTypeOfQuality().getName().toLowerCase())) {
                    if (splittedMessage.getAnimeName().contains(anime.getAnimeName().toLowerCase())) {
                        // TODO MATCH WITH BOT IF CHOSEN
                        // check if user specified bot from who our bot should be downloading
                        message = receivedMessage.substring(receivedMessage.lastIndexOf("/msg"));
                        break;
                    }
                }
            }

            if (message != null) {
                // TEST IF ANIME ISN'T CURRENTLY BEING DOWNLOADED
                if (currentlyDownloading != null && splittedMessage.getAnimeName().contains(currentlyDownloading.getAnimeName())) {
                    //TODO IF EXISTING FILE < ACTUAL FILE SIZE
                    logger.debug("This anime {}, is currently being downloaded", splittedMessage.getAnimeName());
                    return;
                }

                //TODO ADD TEST IF ANIME ISN'T IN QUEUE

                String animeName = null;
                for (WCMAnime anime : workPlace.getAnimeList().getAnimeList()) {
                    if (receivedMessage.contains(anime.getAnimeName().toLowerCase())) {
                        animeName = anime.getAnimeName();
                        break;
                    }
                }
                if (animeName == null)
                    return;
                File folder = new File(downloadFolder + "/" + animeName);
                File[] listOfFiles = folder.listFiles();
                assert listOfFiles != null;
                if (folder.exists()) {
                    for (File file : listOfFiles) {
                        if (receivedMessage.contains(file.getName().toLowerCase())) {
                            logger.debug("This anime {}, is already downloaded", splittedMessage.getAnimeName());
                            return;
                        }
                    }
                }

                /*alreadyDownloadingAnime.add(new AlreadyDownloadingAnime(
                        splittedMessage.getDownloadMessage().getMessage(),
                        splittedMessage.getDownloadMessage().getBotName()));*/

                if (fileTransfer != null && fileTransfer.getFileTransferStatus().isAlive()) {
                    downloadQueue.add(new DownloadMessage(
                            splittedMessage.getDownloadMessage().getBotName(),
                            splittedMessage.getDownloadMessage().getMessage()));
                }
                else {
                    botCommands.sendMessage(
                            splittedMessage.getDownloadMessage().getBotName(),
                            splittedMessage.getDownloadMessage().getMessage());
                    currentlyDownloading = new DownloadMessage(
                            splittedMessage.getDownloadMessage().getBotName(),
                            splittedMessage.getDownloadMessage().getMessage(),
                            splittedMessage.getAnimeName());
                }
                for (DownloadMessage downloadMessage : downloadQueue)
                    logger.info(downloadMessage.getMessage());
            }
        }
    }

    @Override
    public void onIncomingFileTransfer(IncomingFileTransferEvent event) throws Exception {
        logger.debug("On incoming file transfer method started");
        super.onIncomingFileTransfer(event);

        String receivedFileName = event.getSafeFilename(), animeName;

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
        testReceivedFile : try {
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

        String downloadFolder = this.downloadFolder + "/" + animeName;
        workPlace.createFolder(downloadFolder);
        Path path = Paths.get( downloadFolder + "/" + receivedFileName);
        
        if (path.toFile().exists()) {
            // Use BasicFileAttributes to find position to resume
            // TODO IF TO TEST IS EXISTING FILE == event.fileSize();
            logger.debug("File already exists, resuming where ended");
            fileTransfer = event.acceptResume(path.toFile(), Files.readAttributes(path, BasicFileAttributes.class).size());
        }
        else {
            logger.debug("Accepting file transfer");
            fileTransfer = event.accept(path.toFile());
        }

        // Give ReceiveFileTransfer to a new tracking thread or block here
        // with a while (fileTransfer.getFileTransferStatus().isFinished()) loop
        Thread thread = new Thread(() -> {
            long fileSize = event.getFilesize();
            while (!fileTransfer.getFileTransferStatus().isFinished()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //logger.debug("Downloaded: {}", fileTransfer.getFileTransferStatus().getBytesTransfered());
                double progress = (double) fileTransfer.getFileTransferStatus().getBytesTransfered()/fileSize;
                workPlace.send("setProgress", new WCMProgress(progress));
            }
            if (fileTransfer.getFileTransferStatus().isSuccessful()) {
                logger.debug("Clearing currently downloading variable");
                currentlyDownloading = null;
                workPlace.increaseAnimeDownload(animeName);
                logger.debug("Increasing anime download successfully");
                if (!downloadQueue.isEmpty()) {
                    logger.debug("Starting another download");
                    botCommands.sendMessage(downloadQueue.remove(0));
                }
            }
        });
        thread.setName("FileTransferStatus");
        thread.start();
        logger.debug("Transfer started");
        fileTransfer.transfer();
    }
}
