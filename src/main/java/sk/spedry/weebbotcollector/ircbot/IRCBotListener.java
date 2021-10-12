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
import sk.spedry.weebbotcollector.util.WCMAnime;
import sk.spedry.weebbotcollector.work.WBCWorkPlace;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class IRCBotListener extends ListenerAdapter {

    @Setter
    private String downloadFolder;
    private final WBCWorkPlace workPlace;
    private final IRCBotCommands botCommands;
    public IRCBotListener(String downloadFolder, WBCWorkPlace workPlace, IRCBotCommands botCommands) {
        this.downloadFolder = downloadFolder;
        this.workPlace = workPlace;
        this.botCommands = botCommands;
    }

    private final Logger logger = LogManager.getLogger(this.getClass());

    List<String> alreadyDownloadingAnime = new ArrayList<String>();

    @Override
    public void onMessage(MessageEvent event) {
        final String receivedMessage = event.getMessage();
        String downloadMessage = null;

        if (logger.isDebugEnabled())
            logger.debug("Received message: " + receivedMessage);

        if (receivedMessage.contains("/MSG")) {
            logger.info("MSG: " + receivedMessage);
            // got through all anime entries in jsonListFile/animeList.json
            for (WCMAnime anime : workPlace.getAnimeList(workPlace.getAnimeListFile()).getAnimeList()) {
                // if anime quality matches
                if (receivedMessage.contains(anime.getTypeOfQuality())) {
                    // if anime name matches
                    if (receivedMessage.contains(anime.getAnimeName())) {
                        // TODO MATCH WITH BOT IF CHOSEN
                        String substring = receivedMessage.substring(receivedMessage.lastIndexOf("/MSG") + 4);
                        // check if user specified bot from who our bot should be downloading
                        if (anime.getBotName() == null) {
                            downloadMessage = substring;
                            break;
                        }
                        else if (receivedMessage.contains(anime.getBotName())) {
                            downloadMessage = substring;
                            break;
                        }
                        else {
                            //TODO DEFAULT OPTION WHERE
                            // if user wants to download from bot he chose, but:
                            // 1. the anime selected by him was not released by his selected bot
                            // 2. he made a grammatical mistake in bot's name
                            logger.warn("Anime {}, you selected was not released by your selected bot: {}", anime.getAnimeName(), anime.getBotName());
                            //TODO send message to client app and maybe store the errors/warning messages somewhere
                        }
                    }
                }
            }
            if (downloadMessage != null) {
                String[] spliced = downloadMessage.split("\\|");
                for (String animeName : alreadyDownloadingAnime) {
                    if (animeName.contains(spliced[0]))
                        return;
                }
                botCommands.sendMessage(spliced[0], spliced[1]);
            }
        }
    }

    @Override
    public void onIncomingFileTransfer(IncomingFileTransferEvent event) throws Exception {
        logger.debug("Incoming file transfer started");
        super.onIncomingFileTransfer(event);
        // Create Path using your download directory
        Path path = Paths.get( downloadFolder + "/" + event.getSafeFilename());

        ReceiveFileTransfer fileTransfer;

        // If the file exists, resume from a position
        if (path.toFile().exists()) {
            // Use BasicFileAttributes to find position to resume
            fileTransfer = event.acceptResume(path.toFile(),
                    Files.readAttributes(path, BasicFileAttributes.class).size());
        }
        // Accept a new file
        else {
            fileTransfer = event.accept(path.toFile());
        }

        // Give ReceiveFileTransfer to a new tracking thread or block here
        // with a while (fileTransfer.getFileTransferStatus().isFinished()) loop
        fileTransfer.transfer();
        logger.debug("Incoming file transfer ended, waiting for new file");
        logger.info("/------------------------------------------------------/");
        logger.info("Anime names before delete:");
        for (String animeName : alreadyDownloadingAnime) {
            logger.info(animeName);
        }
        alreadyDownloadingAnime.removeIf(animeName -> event.getSafeFilename().contains(animeName));
        logger.info("Anime names after delete:");
        for (String animeName : alreadyDownloadingAnime) {
            logger.info(animeName);
        }
        logger.info("/------------------------------------------------------/");
    }

}
