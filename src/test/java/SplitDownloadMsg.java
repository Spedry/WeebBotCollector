import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SplitDownloadMsg {

    private static final Logger logger = LogManager.getLogger(split.class);
    public static void main(String[] args) {

        String receivedMessage = "From #SubsPlease, come join the one stop shop for new anime * [1.1G] * [SubsPlease] The Defective - 13 (540p) [4C3303CD].mkv * /msg CR-HOLLAND|NEW xdcc send #18838";
        String[] spliced = receivedMessage.split("\\*");
        for (int i = 0; i < spliced.length; i++) {
            logger.info("Splice num: {} - {}", i, spliced[i]);
        }

        String[] spliced2 = spliced[3].trim().split(" ");
        for (int i = 0; i < spliced2.length; i++) {
            logger.info("Splice num: {} - {}", i, spliced2[i]);
        }
        String botName = spliced2[1];
        String message = spliced2[2] + " " + spliced2[3] + " " + spliced2[4];
        logger.debug("Spliced downloadMessage: bot[{}] anime[{}]",botName, message);
    }
}
