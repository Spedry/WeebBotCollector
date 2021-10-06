package sk.spedry.weebbotcollector.work;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.spedry.weebbotcollector.ircbot.WBCService;
import sk.spedry.weebbotcollector.ircbot.util.BotInfo;
import sk.spedry.weebbotcollector.util.WCMessage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingDeque;

public class WBCMessageHandler {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Socket socket;
    private final LinkedBlockingDeque<WCMessage> WCMessageQueue;
    private final WBCWorkPlace work;
    private final WBCService service;

    public WBCMessageHandler(Socket socket) {
        this.socket = socket;
        WBCMessageReceiver messageReceiver = new WBCMessageReceiver(socket);
        Thread wbcMessageReceiverThread = new Thread(messageReceiver);
        wbcMessageReceiverThread.setDaemon(true);
        wbcMessageReceiverThread.start();
        this.WCMessageQueue = messageReceiver.getMessageQueue();
        WBCWorkPlace workPlace = null;
        try {
            workPlace = new WBCWorkPlace(new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8));
        } catch (IOException e) {
            logger.error("Couldn't get output stream", e);
        }
        this.work = workPlace;

        service = new WBCService();
        service.createBot(new BotInfo("Spedry", "irc.rizon.net", "#NIBL", "/media/spedry/RaspCloud/CloudShare"),workPlace);

    }

    public void messageHandler() {
        loop: while (true) {
            try {
                WCMessage wcMessage = WCMessageQueue.take();
                logger.debug("The message id is: " + wcMessage.getMessageId());
                logger.debug("The content of WCMessage body is: " + wcMessage.getMessageBody());
                switch (wcMessage.getMessageId()) {
                    case "addServer":
                        work.addServer(wcMessage);
                        break;
                    case "getServerList":
                        work.getServerList(wcMessage);
                        break;
                    case "addNewAnimeEntry":
                        work.addNewAnimeEntry(wcMessage);
                        break;
                    case "getAnimeList":
                        work.getAnimeList(wcMessage);
                        break;
                    case "setSetup":
                        work.setSetup(wcMessage);
                        break;
                    case "getSetup":
                        work.getSetup(wcMessage);
                        break;


                    case "clientDisconnected":
                        logger.debug(wcMessage.getMessageBody());
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
