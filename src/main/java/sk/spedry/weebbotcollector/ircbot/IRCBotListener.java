package sk.spedry.weebbotcollector.ircbot;

import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pircbotx.dcc.ReceiveFileTransfer;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.IncomingFileTransferEvent;
import org.pircbotx.hooks.events.MessageEvent;
import sk.spedry.weebbotcollector.util.WCMAnime;
import sk.spedry.weebbotcollector.work.WBCWorkPlace;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

public class IRCBotListener extends ListenerAdapter {

    @Setter
    private String downloadFolder;
    private WBCWorkPlace workPlace;
    private IRCBotCommands botCommands;
    public IRCBotListener(String downloadFolder, WBCWorkPlace workPlace, IRCBotCommands botCommands) {
        this.downloadFolder = downloadFolder;
        this.workPlace = workPlace;
        this.botCommands = botCommands;
    }

    private final Logger logger = LogManager.getLogger(this.getClass());

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
                        // TODO option to choose server
                        downloadMessage = receivedMessage.substring(receivedMessage.lastIndexOf("/MSG") + 4);
                        break;
                    }
                }
            }
            if (downloadMessage != null) {
                String[] spliced = downloadMessage.split("\\|");
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
    }

}
