import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sk.spedry.weebbotcollector.ircbot.util.SplitAlreadyReleased;
import sk.spedry.weebbotcollector.ircbot.util.SplitNewRelease;

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

    private void splitMessage() {
        SplitAlreadyReleased alreadyReleased = new SplitAlreadyReleased("05[10310M05] 14[SubsPlease] Tensei Kenja no Isekai Life - 02 (1080p) [E7B452D9].mkv  /MSG CR-ARUTHA|NEW XDCC SEND 4277");
        logger.info("Size: " + alreadyReleased.getSize());
        //logger.info("AnimeName: " + alreadyReleased.getAnimeFileName());
        logger.info("WholeMsg: " + alreadyReleased.getDownloadMessage().getWholeMessage());
        logger.info("Bot: " + alreadyReleased.getDownloadMessage().getBotName());
        logger.info("Msg: " + alreadyReleased.getDownloadMessage().getMessage());
        logger.info("AnimeFileName: " + alreadyReleased.getDownloadMessage().getAnimeFileName());
        logger.info("AnimeName: " + alreadyReleased.getDownloadMessage().getAnimeName());
        logger.info("AnimeQ: " + alreadyReleased.getDownloadMessage().getAnimeQuality());
        logger.info("EpNum: " + alreadyReleased.getDownloadMessage().getEpisodeNumber().getEpisodeNumber());

        SplitNewRelease newRelease = new SplitNewRelease("<SubsPlease|NEW> Added * [702M] * [SubsPlease] Sakugan - 06 (720p) [7E3492A1].mkv * /MSG SubsPlease|NEW XDCC SEND 147");

        logger.info("Info: " + newRelease.getInfo());
        logger.info("Size: " + newRelease.getSize());
        //logger.info("AnimeName: " + newRelease.getAnimeFileName());
        logger.info("WholeMsg: " + newRelease.getDownloadMessage().getWholeMessage());
        logger.info("Bot: " + newRelease.getDownloadMessage().getBotName());
        logger.info("Msg: " + newRelease.getDownloadMessage().getMessage());
        logger.info("AnimeFileName: " + newRelease.getDownloadMessage().getAnimeFileName());
        logger.info("AnimeName: " + newRelease.getDownloadMessage().getAnimeName());
        logger.info("AnimeQ: " + newRelease.getDownloadMessage().getAnimeQuality());
        logger.info("EpNum: " + newRelease.getDownloadMessage().getEpisodeNumber().getEpisodeNumber());
    }
}
