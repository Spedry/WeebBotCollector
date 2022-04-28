package sk.spedry.weebbotcollector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.spedry.weebbotcollector.ircbot.WBCService;
import sk.spedry.weebbotcollector.work.WBCMessageHandler;
import sk.spedry.weebbotcollector.work.WBCWorkPlace;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Date;

public class WBCApplication {

    //netstat -ano | findstr :<PORT> == netstat -ano | findstr :50000
    //taskkill /PID <PID> /F == taskkill /PID XXXXXX /F

    private final int port = 50000;

    private final Logger logger = LogManager.getLogger(this.getClass());

    public static void main(String[] args) {
        WBCApplication wbcApplication = new WBCApplication();
        wbcApplication.startServer();
    }

    public void startServer() {
        logger.traceEntry();
        try {
            logger.trace("Starting server...");
            ServerSocket sSocket = new ServerSocket(port);
            InetAddress ip = InetAddress.getLocalHost();
            String hostName = ip.getHostName();
            logger.info("Info: " +
                    "\n\tIP: " + ip +
                    "\n\tName of host: " + hostName +
                    "\n\tWaiting for input on port: " + port + "...");

            WBCWorkPlace work = new WBCWorkPlace();

            while (true) {
                WBCMessageHandler wbcMessageHandler = new WBCMessageHandler(sSocket.accept(), work);

                logger.info("Connection established. [{}])", new Date());
                wbcMessageHandler.messageHandler();
                logger.info("User disconnected. [{}]", new Date());
            }
        } catch (IOException e) {
            logger.error("Starting server", e);
        }
        logger.traceExit();
    }
}
