package sk.spedry.weebbotcollector.ircbot;

import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.Message;
import org.pircbotx.dcc.ReceiveFileTransfer;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.IncomingFileTransferEvent;
import org.pircbotx.hooks.events.MessageEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

public class IRCBotListener extends ListenerAdapter {

    @Setter
    private String downloadFolder;

    public IRCBotListener(String downloadFolder) {
        this.downloadFolder = downloadFolder;
    }

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Override
    public void onMessage(MessageEvent event) {
        logger.info("RECEIVED MESSAGE: " + event.getMessage());
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
