import com.google.gson.Gson;
import sk.spedry.weebbotcollector.ircbot.IRCBot;
import sk.spedry.weebbotcollector.ircbot.util.SplitAlreadyReleased;
import sk.spedry.weebbotcollector.properties.Configuration;
import sk.spedry.weebbotcollector.work.WBCWorkPlace;

public class EmptyTest {



    public static void main(String[] args) throws InterruptedException {
        Configuration configuration = new Configuration();

        for (String s : configuration.getAllWhoHasAccess())
            System.out.println(s);
    }
}