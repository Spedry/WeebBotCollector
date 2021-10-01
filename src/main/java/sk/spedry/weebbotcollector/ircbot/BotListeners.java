package sk.spedry.weebbotcollector.ircbot;

import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pircbotx.dcc.ReceiveFileTransfer;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.IncomingFileTransferEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

public class BotListeners extends ListenerAdapter {

    private final Logger logger = LogManager.getLogger(this.getClass());


    @Override
    public void onPrivateMessage(PrivateMessageEvent event){
        //When someone says ?helloworld respond with "Hello World"
        if (event.getMessage().contains("hi")) {
            event.respond("/MSG Spedry hi!!!");
            logger.info("received msg: " + event.getMessage());
        }
    }

    @Override
    public void onIncomingFileTransfer(IncomingFileTransferEvent event) throws Exception {
        logger.info("RECEIVING FILE");
        super.onIncomingFileTransfer(event);
        // Create Path using your download directory
        Path path = Paths.get("Y:/TEST/" + event.getSafeFilename());

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
        logger.info("RECEIVING FILE ENDED");
    }
}
