import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pircbotx.hooks.events.MessageEvent;
import sk.spedry.weebbotcollector.util.WCMAnime;
import sk.spedry.weebbotcollector.work.WBCWorkPlace;

public class GetMSGFromString {

    private final Logger logger = LogManager.getLogger(this.getClass());

    WBCWorkPlace workPlace = new WBCWorkPlace(null);

    String msg = "From #SubsPlease, come join the one stop shop for new anime * [295M] * [SubsPlease] Re-Main - 12 (480p) [30B51151].mkv * 07/MSG CR-HOLLAND-IPv6|NEW XDCC SEND 18562";

    public static void main(String[] args) {
        new GetMSGFromString();
    }

    public GetMSGFromString() {
        onMessage();
    }

    public void onMessage() {
        final String receivedMessage = msg;
        String downloadMessage = null;
        if (receivedMessage.contains("/MSG")) {
            logger.info("MSG: " + receivedMessage);

            for (WCMAnime anime : workPlace.getAnimeList(workPlace.getAnimeListFile()).getAnimeList()) {
                if (receivedMessage.contains(anime.getTypeOfQuality())) {
                    if (receivedMessage.contains(anime.getAnimeName())) {
                        // TODO option to choose server
                        downloadMessage = receivedMessage.substring(receivedMessage.lastIndexOf("/MSG") + 4);
                        break;
                    }
                }
            }

            String[] splited = downloadMessage.split("\\|");
            logger.info("Target: " + splited[0]);
            logger.info("Msg: " + splited[1]);
        }
    }

}
