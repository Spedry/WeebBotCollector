package sk.spedry.weebbotcollector.work;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.spedry.weebbotcollector.util.WCMessage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingDeque;

public class WBCMessageHandler {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final LinkedBlockingDeque<WCMessage> WCMessageQueue;
    private final WBCWorkPlace work;

    public WBCMessageHandler(Socket socket, WBCWorkPlace work) {
        logger.trace("Creating Message handler");
        WBCMessageReceiver messageReceiver = new WBCMessageReceiver(socket);
        this.work = work;
        Thread wbcMessageReceiverThread = new Thread(messageReceiver);
        wbcMessageReceiverThread.setDaemon(true);
        wbcMessageReceiverThread.setName(messageReceiver.getClass().getSimpleName());
        wbcMessageReceiverThread.start();
        this.WCMessageQueue = messageReceiver.getMessageQueue();
        PrintWriter printWriter = null;
        try {
            printWriter = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        work.setOut(printWriter);
    }

    public void messageHandler() {
        loop: while (true) {
            try {
                WCMessage wcMessage = WCMessageQueue.take();
                logger.debug("The message id is: " + wcMessage.getMessageId());
                logger.debug("The content of WCMessage body is: " + wcMessage.getMessageBody());
                switch (wcMessage.getMessageId()) {
                    case "setSetup":
                        work.setSetup(wcMessage);
                    case "getSetup":
                        work.getSetup(wcMessage);
                        break;
                    case "startIRCBot":
                        work.startIRCBot(wcMessage);
                        break;
                    case "addNewAnimeEntry":
                        work.addNewAnimeEntry(wcMessage);
                    case "getAnimeList":
                        work.getAnimeList(wcMessage);
                        break;
                    case "updateAnime":
                        work.updateAnime(wcMessage);
                        break;
                    case "removeAnimeFromList":
                        work.removeAnimeFromList(wcMessage);
                        break;
                    case "getDownloadQueueList":
                        //TODO ON CLIENT SIDE
                        work.getDownloadQueueList(wcMessage);
                        break;
                    case "getAlreadyReleasedQueueList":
                        //TODO ON CLIENT SIDE
                        work.getAlreadyReleasedQueueList(wcMessage);
                        break;
                    case "getCurrentlyDownloading":
                        //TODO ON CLIENT SIDE
                        work.getCurrentlyDownloadingAnime(wcMessage);
                        break;


                    case "clientDisconnected":
                        logger.debug(wcMessage.getMessageBody());
                        // making printWriter null to prevent sending messages to already closed connection
                        work.setOut(null);
                        break loop;

                    default:
                        logger.warn("Received unknown id");
                        break;
                } // SWITCH
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
