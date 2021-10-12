package sk.spedry.weebbotcollector.work;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;

public class WBCMessageSender {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Getter
    @Setter
    private PrintWriter out;

    public WBCMessageSender() {
    }

    public <T> void sendMessage(T object) {
        try {
            String message = new Gson().toJson(object);
            if (message == null) {
                throw new Exception("Couldn't transform object into json text");
            }
            sendMessage(message);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void sendMessage(String message) {
        if (out != null)
            out.println(message);
        else
            logger.error("Print writer is null");
    }
}
