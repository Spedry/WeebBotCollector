import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class split {
    private static final Logger logger = LogManager.getLogger(split.class);
    public static void main(String[] args) {

        String receivedMessage = "<Ginpachi-Sensei> [https://gin.sadaharu.eu] - [1.1G] - [SubsPlease] Selection Project - 03 (1080p) [4C3303CD].mkv - /MSG Ginpachi-Sensei xdcc send 3171";
        String downloadMessage;
        downloadMessage = receivedMessage.substring(receivedMessage.lastIndexOf("/MSG"));
        String[] spliced = downloadMessage.split(" ");
        for (int i = 0; i < spliced.length; i++) {
            logger.info("Splice num: {} - {}", i, spliced[i]);
        }
        String botName = spliced[1];
        String message = spliced[2] + " " + spliced[3] + " " + spliced[4];
        logger.debug("Spliced downloadMessage: bot[{}] anime[{}]",botName, message);
    }
}
