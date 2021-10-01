package sk.spedry.weebbotcollector.work;

import com.google.gson.Gson;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.spedry.weebbotcollector.util.WCMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingDeque;

public class WBCMessageReceiver implements Runnable {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private BufferedReader br;
    @Getter
    private LinkedBlockingDeque<WCMessage> messageQueue;

    public WBCMessageReceiver(Socket socket) {
        try {
            this.br = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.messageQueue = new LinkedBlockingDeque<WCMessage>();
        } catch (IOException e) {
            logger.error("Get input stream", e);
        }
    }

    @Override
    public void run() {
        try {
            String data;
            logger.debug("Starting receiving messages");
            while ((data = br.readLine()) != null) {
                messageQueue.add(new Gson().fromJson(data, WCMessage.class));
            }
            logger.debug("Receiving messages ended");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            messageQueue.add(new WCMessage("clientDisconnected"));
        }
    }
}
