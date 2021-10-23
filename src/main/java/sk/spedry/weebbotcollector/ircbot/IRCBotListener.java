package sk.spedry.weebbotcollector.ircbot;

import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pircbotx.dcc.ReceiveFileTransfer;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.IncomingFileTransferEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import sk.spedry.weebbotcollector.ircbot.util.AlreadyDownloadingAnime;
import sk.spedry.weebbotcollector.util.WCMAnime;
import sk.spedry.weebbotcollector.util.WCMProgress;
import sk.spedry.weebbotcollector.work.WBCWorkPlace;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class IRCBotListener extends ListenerAdapter {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Setter
    private String downloadFolder;
    private final WBCWorkPlace workPlace;
    private final IRCBotCommands botCommands;
    //TODO think about this
    private List<AlreadyDownloadingAnime> alreadyDownloadingAnime = new ArrayList<AlreadyDownloadingAnime>();
    @Setter
    // TODO ADD OPTIONS TO SET THIS VALUE FROM APP/INIT THIS VALUE WHEN BOT IS CREATED
    // one GB = 1 000 000 00 bytes for info
    private long maxFileSize = 2000000000; //2gb default

    public IRCBotListener(String downloadFolder, WBCWorkPlace workPlace, IRCBotCommands botCommands) {
        this.downloadFolder = downloadFolder;
        this.workPlace = workPlace;
        this.botCommands = botCommands;
    }
     @Override
    public void onGenericMessage(GenericMessageEvent event) throws Exception {
        logger.debug("Generic message: {}", event.getMessage());
    }

    @Override
    public void onMessage(MessageEvent event) {
        final String receivedMessage = event.getMessage().toLowerCase();
        String downloadMessage = null;

        if (logger.isDebugEnabled())
            logger.debug("Received message: " + receivedMessage);

        if (receivedMessage.contains("/msg")) {
            logger.info("MSG: " + receivedMessage);
            logger.debug("Going through all anime entries in jsonListFile: animeList.json");
            for (WCMAnime anime : workPlace.getAnimeList().getAnimeList()) {
                logger.debug("Testing if anime quality matches");
                if (receivedMessage.contains(anime.getTypeOfQuality().getName().toLowerCase())) {
                    logger.info("Testing if anime name matches");
                    if (receivedMessage.contains(anime.getAnimeName().toLowerCase())) {
                        // TODO MATCH WITH BOT IF CHOSEN
                        // check if user specified bot from who our bot should be downloading
                        downloadMessage = receivedMessage.substring(receivedMessage.lastIndexOf("/msg"));
                        break;
                    }
                }
            }
            if (downloadMessage != null) {
                String[] spliced = botCommands.splitDownloadMessage(downloadMessage);
                String botName = spliced[1];
                String message = spliced[2];
                logger.debug("Spliced downloadMessage: bot[{}] anime[{}]", botName, message);
                for (AlreadyDownloadingAnime ADA : alreadyDownloadingAnime) {
                    if (ADA.getMessage().contains(message))
                        return;
                }
                alreadyDownloadingAnime.add(new AlreadyDownloadingAnime(message, botName));
                botCommands.sendMessage(botName, message);
            }
            else {
                logger.warn("DownloadMessage was null");
            }
        }
    }

    //public void onPrivateMessage(PrivateMessageEvent event) {
        // TODO OPTION TO DOWNLOAD ANIME BY SENDING PRIVATE MESSAGE TO BOT
    //}
    @Override
    public void onPrivateMessage(PrivateMessageEvent event) {
        final String receivedMessage = event.getMessage().toLowerCase();
        String downloadMessage = null;

        if (logger.isDebugEnabled())
            logger.debug("Received message: " + receivedMessage);

        if (receivedMessage.contains("/msg")) {
            logger.info("MSG: " + receivedMessage);
            logger.debug("Going through all anime entries in jsonListFile: animeList.json");
            for (WCMAnime anime : workPlace.getAnimeList().getAnimeList()) {
                logger.debug("Testing if anime quality matches");
                if (receivedMessage.contains(anime.getTypeOfQuality().getName().toLowerCase())) {
                    logger.info("Testing if anime name matches");
                    if (receivedMessage.contains(anime.getAnimeName().toLowerCase())) {
                        // TODO MATCH WITH BOT IF CHOSEN
                        // check if user specified bot from who our bot should be downloading
                        downloadMessage = receivedMessage.substring(receivedMessage.lastIndexOf("/msg"));
                        break;
                    }
                }
            }
            if (downloadMessage != null) {
                String[] spliced = botCommands.splitDownloadMessage(downloadMessage);
                String botName = spliced[0];
                String message = spliced[1];
                logger.debug("Spliced downloadMessage: bot[{}] anime[{}]", botName, message);
                for (AlreadyDownloadingAnime ADA : alreadyDownloadingAnime) {
                    if (ADA.getMessage().contains(message))
                        return;
                }
                alreadyDownloadingAnime.add(new AlreadyDownloadingAnime(message, botName));
                botCommands.sendMessage(botName, message);
            }
            else {
                logger.warn("DownloadMessage was null");
            }
        }
    }

    @Override
    public void onIncomingFileTransfer(IncomingFileTransferEvent event) throws Exception {
        logger.debug("On incoming file transfer started");
        super.onIncomingFileTransfer(event);

        String receivedFileName = event.getSafeFilename(), animeName;

        logger.debug("Testing if maxFileSize isn't null");
        if (maxFileSize == 0) {
            logger.error("Variable maxFileSite in null");
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
        ReceiveFileTransfer fileTransfer;
        
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
                logger.debug("Downloaded: {}", fileTransfer.getFileTransferStatus().getBytesTransfered());
                double progress = (double) fileTransfer.getFileTransferStatus().getBytesTransfered()/fileSize;
                workPlace.send("setProgress", new WCMProgress(progress));
            }
            if (fileTransfer.getFileTransferStatus().isSuccessful()) {
                workPlace.increaseAnimeDownload(animeName);
                logger.debug("Increasing anime download successfully");
            }
        });
        thread.setName("FileTransferStatus");
        thread.start();
        logger.debug("Transfer started");
        fileTransfer.transfer();
    }
}
